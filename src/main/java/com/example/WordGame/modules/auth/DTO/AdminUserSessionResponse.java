package com.example.WordGame.modules.auth.DTO;

import java.time.LocalDateTime;
import java.util.UUID;

public record AdminUserSessionResponse(
        UUID id,
        Long userId,
        String username,
        String email,
        String deviceId,
        String deviceName,
        LocalDateTime createdAt,
        LocalDateTime lastUsedAt,
        LocalDateTime expiresAt,
        LocalDateTime revokedAt,
        String status) {
}