package com.example.WordGame.modules.auth.service;

import com.example.WordGame.modules.auth.entity.UserSession;
import com.example.WordGame.modules.auth.DTO.AdminUserSessionResponse;
import com.example.WordGame.modules.auth.repository.AuthUtil;
import com.example.WordGame.modules.auth.repository.UserSessionRepository;
import com.example.WordGame.modules.roles.user.Entities.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserSessionService {
    private final UserSessionRepository sessionRepository;
    private final AuthUtil authUtil;

    @Transactional
    public IssuedAuthTokens createSession(User user, String deviceId, String deviceName) {
        UUID sessionId = UUID.randomUUID();
        String accessToken = authUtil.generateToken(user, sessionId);
        String refreshToken = authUtil.generateRefreshToken(user, sessionId);

        UserSession session = new UserSession();
        session.setId(sessionId);
        session.setUser(user);
        session.setRefreshTokenHash(hashToken(refreshToken));
        session.setDeviceId(limit(deviceId, 128));
        session.setDeviceName(limit(deviceName, 160));
        session.setExpiresAt(expirationAsUtc(refreshToken));
        session.setLastUsedAt(now());
        sessionRepository.save(session);
        return new IssuedAuthTokens(accessToken, refreshToken, sessionId);
    }

    @Transactional
    public IssuedAuthTokens rotate(String refreshToken) {
        Claims claims = parseRefreshClaims(refreshToken);
        UUID sessionId = parseSessionId(claims);
        long userId = parseUserId(claims);
        UserSession session = sessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(this::invalidSession);
        LocalDateTime now = now();

        if (!session.getUser().getId().equals(userId)
                || session.getRevokedAt() != null
                || !session.getExpiresAt().isAfter(now)
                || !MessageDigest.isEqual(
                        session.getRefreshTokenHash().getBytes(StandardCharsets.US_ASCII),
                        hashToken(refreshToken).getBytes(StandardCharsets.US_ASCII))) {
            throw invalidSession();
        }

        User user = session.getUser();
        if (!Boolean.TRUE.equals(user.getIsActive())) throw invalidSession();

        String accessToken = authUtil.generateToken(user, sessionId);
        String rotatedRefreshToken = authUtil.generateRefreshToken(user, sessionId);
        session.setRefreshTokenHash(hashToken(rotatedRefreshToken));
        session.setLastUsedAt(now);
        session.setExpiresAt(expirationAsUtc(rotatedRefreshToken));
        return new IssuedAuthTokens(accessToken, rotatedRefreshToken, sessionId);
    }

    @Transactional
    public void revoke(UUID sessionId, Long userId) {
        if (sessionId == null || userId == null) return;
        sessionRepository.findByIdAndUserId(sessionId, userId).ifPresent(session -> {
            if (session.getRevokedAt() == null) session.setRevokedAt(now());
        });
    }

    @Transactional
    public int revokeAll(Long userId) {
        return sessionRepository.revokeAllActiveForUser(userId, now());
    }

    @Transactional(readOnly = true)
    public Page<AdminUserSessionResponse> findAdminSessions(String status, Pageable pageable) {
        LocalDateTime now = now();
        Page<UserSession> sessions = switch (status.toUpperCase(java.util.Locale.ROOT)) {
            case "ACTIVE" -> sessionRepository
                    .findByRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(now, pageable);
            case "REVOKED" -> sessionRepository.findByRevokedAtIsNotNullOrderByRevokedAtDesc(pageable);
            case "EXPIRED" -> sessionRepository
                    .findByRevokedAtIsNullAndExpiresAtLessThanEqualOrderByExpiresAtDesc(now, pageable);
            case "ALL" -> sessionRepository.findAllByOrderByCreatedAtDesc(pageable);
            default -> throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "status must be ACTIVE, REVOKED, EXPIRED, or ALL.");
        };
        return sessions.map(session -> toAdminResponse(session, now));
    }

    @Transactional
    public AdminUserSessionResponse revokeForAdmin(UUID sessionId) {
        UserSession session = sessionRepository.findByIdForUpdate(sessionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Session not found."));
        if (session.getRevokedAt() == null) session.setRevokedAt(now());
        return toAdminResponse(session, now());
    }

    private Claims parseRefreshClaims(String token) {
        try {
            Claims claims = authUtil.parseClaims(token);
            if (!"refresh".equals(claims.get("type", String.class))) throw invalidSession();
            return claims;
        } catch (JwtException | IllegalArgumentException exception) {
            throw invalidSession();
        }
    }

    private UUID parseSessionId(Claims claims) {
        try {
            return UUID.fromString(claims.get("sessionId", String.class));
        } catch (RuntimeException exception) {
            throw invalidSession();
        }
    }

    private long parseUserId(Claims claims) {
        try {
            return Long.parseLong(claims.get("userId", String.class));
        } catch (RuntimeException exception) {
            throw invalidSession();
        }
    }

    private String hashToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    private LocalDateTime expirationAsUtc(String token) {
        return LocalDateTime.ofInstant(authUtil.getExpirationDateFromToken(token).toInstant(), ZoneOffset.UTC);
    }

    private LocalDateTime now() {
        return LocalDateTime.now(ZoneOffset.UTC);
    }

    private String limit(String value, int maxLength) {
        if (value == null || value.isBlank()) return null;
        return value.length() <= maxLength ? value : value.substring(0, maxLength);
    }

    private ResponseStatusException invalidSession() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired session.");
    }

    private AdminUserSessionResponse toAdminResponse(UserSession session, LocalDateTime now) {
        String status = session.getRevokedAt() != null
                ? "REVOKED"
                : session.getExpiresAt().isAfter(now) ? "ACTIVE" : "EXPIRED";
        User user = session.getUser();
        return new AdminUserSessionResponse(
                session.getId(), user.getId(), user.getUsername(), user.getEmail(),
                session.getDeviceId(), session.getDeviceName(), session.getCreatedAt(),
                session.getLastUsedAt(), session.getExpiresAt(), session.getRevokedAt(), status);
    }
}