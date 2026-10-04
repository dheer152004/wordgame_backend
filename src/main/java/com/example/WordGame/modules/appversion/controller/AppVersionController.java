package com.example.WordGame.modules.appversion.controller;

import com.example.WordGame.modules.appversion.dto.AppVersionResponse;
import com.example.WordGame.modules.appversion.service.AppVersionPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/v1/app/version", produces = "application/json")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AppVersionController {
    private final AppVersionPolicyService service;

    @GetMapping
    public ResponseEntity<AppVersionResponse> getVersionPolicy(@RequestParam String platform) {
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(service.findPublicPolicy(platform));
    }
}