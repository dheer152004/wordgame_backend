package com.example.WordGame.modules.auth.service;

import com.example.WordGame.modules.auth.entity.UserSession;
import com.example.WordGame.modules.auth.DTO.AdminUserSessionResponse;
import com.example.WordGame.modules.auth.repository.AuthUtil;
import com.example.WordGame.modules.auth.repository.UserSessionRepository;
import com.example.WordGame.modules.roles.user.Entities.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserSessionServiceTest {
    @Mock
    private UserSessionRepository repository;

    private AuthUtil authUtil;
    private UserSessionService service;
    private User user;

    @BeforeEach
    void setUp() {
        authUtil = new AuthUtil();
        ReflectionTestUtils.setField(authUtil, "secretKey", "test-secret-key-123456789012345678901234567890");
        ReflectionTestUtils.setField(authUtil, "accessExpirationMs", 60 * 60 * 1000L);
        ReflectionTestUtils.setField(authUtil, "refreshExpirationMs", 120L * 24 * 60 * 60 * 1000);
        service = new UserSessionService(repository, authUtil);
        user = new User();
        user.setId(9L);
        user.setUsername("session-user");
        user.setIsActive(true);
    }

    @Test
    void createSessionStoresOnlyRefreshTokenHash() throws Exception {
        var tokens = service.createSession(user, "device-1", "Android device");
        ArgumentCaptor<UserSession> sessionCaptor = ArgumentCaptor.forClass(UserSession.class);
        verify(repository).save(sessionCaptor.capture());

        UserSession session = sessionCaptor.getValue();
        Claims accessClaims = authUtil.parseClaims(tokens.accessToken());
        Claims refreshClaims = authUtil.parseClaims(tokens.refreshToken());
        assertEquals(tokens.sessionId(), session.getId());
        assertEquals("access", accessClaims.get("type", String.class));
        assertEquals("refresh", refreshClaims.get("type", String.class));
        assertEquals(tokens.sessionId().toString(), accessClaims.get("sessionId", String.class));
        assertEquals(hash(tokens.refreshToken()), session.getRefreshTokenHash());
        assertNotEquals(tokens.refreshToken(), session.getRefreshTokenHash());
    }

    @Test
    void rotationReplacesHashAndRejectsTheOldRefreshToken() throws Exception {
        UUID sessionId = UUID.randomUUID();
        String oldRefreshToken = authUtil.generateRefreshToken(user, sessionId);
        UserSession session = new UserSession();
        session.setId(sessionId);
        session.setUser(user);
        session.setRefreshTokenHash(hash(oldRefreshToken));
        session.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(30));
        when(repository.findByIdForUpdate(sessionId)).thenReturn(Optional.of(session));

        IssuedAuthTokens rotated = service.rotate(oldRefreshToken);

        assertEquals(sessionId, rotated.sessionId());
        assertNotEquals(oldRefreshToken, rotated.refreshToken());
        assertEquals(hash(rotated.refreshToken()), session.getRefreshTokenHash());
        assertThrows(ResponseStatusException.class, () -> service.rotate(oldRefreshToken));
    }

    @Test
    void adminListingMapsSafeSessionFieldsAndStatus() {
        UserSession session = new UserSession();
        session.setId(UUID.randomUUID());
        session.setUser(user);
        session.setRefreshTokenHash("do-not-return-this-hash");
        session.setDeviceName("Android device");
        session.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(1));
        when(repository.findByRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new PageImpl<>(List.of(session)));

        var result = service.findAdminSessions("ACTIVE", PageRequest.of(0, 20));
        AdminUserSessionResponse response = result.getContent().get(0);

        assertEquals("ACTIVE", response.status());
        assertEquals("session-user", response.username());
        Assertions.assertFalse(java.util.Arrays.stream(AdminUserSessionResponse.class.getRecordComponents())
                .anyMatch(component -> component.getName().equals("refreshTokenHash")));
    }

    @Test
    void adminCanRevokeSession() {
        UUID sessionId = UUID.randomUUID();
        UserSession session = new UserSession();
        session.setId(sessionId);
        session.setUser(user);
        session.setExpiresAt(LocalDateTime.now(ZoneOffset.UTC).plusDays(1));
        when(repository.findByIdForUpdate(sessionId)).thenReturn(Optional.of(session));

        AdminUserSessionResponse response = service.revokeForAdmin(sessionId);

        assertEquals("REVOKED", response.status());
        Assertions.assertNotNull(session.getRevokedAt());
    }

    private String hash(String token) throws Exception {
        byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(token.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digest);
    }
}