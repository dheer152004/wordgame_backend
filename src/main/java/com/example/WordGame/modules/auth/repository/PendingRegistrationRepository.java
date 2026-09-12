package com.example.WordGame.modules.auth.repository;

import com.example.WordGame.modules.auth.entity.PendingRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PendingRegistrationRepository extends JpaRepository<PendingRegistration, Long> {

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    Optional<PendingRegistration> findByVerificationToken(String verificationToken);

    List<PendingRegistration> findAllByVerificationExpiresAtAfterOrderByIdAsc(LocalDateTime now);
}