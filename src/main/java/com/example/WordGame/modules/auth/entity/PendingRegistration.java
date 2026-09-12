package com.example.WordGame.modules.auth.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "pending_registrations")
@Getter
@Setter
@NoArgsConstructor
public class PendingRegistration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(nullable = false, length = 20)
    private String role = "USER";

    @Column(name = "verification_token", nullable = false, unique = true, length = 255)
    private String verificationToken;

    @Column(name = "verification_expires_at", nullable = false)
    private LocalDateTime verificationExpiresAt;

    @Column(name = "accepted_document_ids", columnDefinition = "TEXT")
    private String acceptedDocumentIds;

    @Column(name = "accepted_from", length = 100)
    private String acceptedFrom;
}