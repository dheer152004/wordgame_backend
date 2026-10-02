package com.example.WordGame.modules.auth.controller;

import com.example.WordGame.modules.auth.DTO.AdminUserSessionResponse;
import com.example.WordGame.modules.auth.service.UserSessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping(value = "/api/v1/admin/user-sessions", produces = "application/json")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserSessionController {
    private final UserSessionService sessionService;

    @GetMapping
    public ResponseEntity<Page<AdminUserSessionResponse>> list(
            @RequestParam(defaultValue = "ACTIVE") String status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        int safePage = Math.max(0, page);
        int safeSize = Math.max(1, Math.min(size, 100));
        PageRequest pageable = PageRequest.of(
                safePage, safeSize, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(sessionService.findAdminSessions(status, pageable));
    }

    @PatchMapping("/{sessionId}/revoke")
    public ResponseEntity<AdminUserSessionResponse> revoke(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(sessionService.revokeForAdmin(sessionId));
    }
}