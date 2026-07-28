package com.example.WordGame.modules.userConsent.controller;

import com.example.WordGame.modules.roles.user.Entities.User;
import com.example.WordGame.modules.userConsent.entity.UserConsent;
import com.example.WordGame.modules.userConsent.service.UserConsentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;
import java.util.Map;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping(value = "/api", produces = "application/json")
@RequiredArgsConstructor
public class UserConsentController {

    private final UserConsentService userConsentService;

    @GetMapping("/users/me/consents")
    public ResponseEntity<?> getMyConsents(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("authenticated", false));
        }
        List<com.example.WordGame.modules.userConsent.DTO.UserConsentResponse> consents = userConsentService.getUserConsents(user.getId());
        return ResponseEntity.ok(Map.of("consents", consents));
    }

    @GetMapping("/admin/users/{userId}/consents")
    public ResponseEntity<?> getUserConsentsForAdmin(@PathVariable Long userId) {
        List<com.example.WordGame.modules.userConsent.DTO.UserConsentResponse> consents = userConsentService.getUserConsents(userId);
        return ResponseEntity.ok(Map.of("userId", userId, "consents", consents));
    }
}
