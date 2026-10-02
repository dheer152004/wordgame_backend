package com.example.WordGame.modules.auth.repository;

import com.example.WordGame.modules.auth.entity.UserSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

public interface UserSessionRepository extends JpaRepository<UserSession, UUID> {
    Page<UserSession> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<UserSession> findByRevokedAtIsNullAndExpiresAtAfterOrderByCreatedAtDesc(
        LocalDateTime now, Pageable pageable);

    Page<UserSession> findByRevokedAtIsNotNullOrderByRevokedAtDesc(Pageable pageable);

    Page<UserSession> findByRevokedAtIsNullAndExpiresAtLessThanEqualOrderByExpiresAtDesc(
        LocalDateTime now, Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from UserSession session where session.id = :id")
    Optional<UserSession> findByIdForUpdate(@Param("id") UUID id);

    Optional<UserSession> findByIdAndUserId(UUID id, Long userId);

    @Modifying
    @Query("update UserSession session set session.revokedAt = :revokedAt "
            + "where session.user.id = :userId and session.revokedAt is null "
            + "and session.expiresAt > :revokedAt")
    int revokeAllActiveForUser(@Param("userId") Long userId, @Param("revokedAt") LocalDateTime revokedAt);
}