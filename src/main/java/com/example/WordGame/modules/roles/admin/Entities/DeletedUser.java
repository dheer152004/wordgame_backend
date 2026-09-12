package com.example.WordGame.modules.roles.admin.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "deleted_users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeletedUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "original_user_id")
    private Long originalUserId;

    private String username;
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "is_guest")
    private Boolean isGuest;

    @Column(name = "email_verified")
    private Boolean emailVerified;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @Column(name = "last_active")
    private LocalDateTime lastActive;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;

    @Column(name = "deletion_reason", columnDefinition = "TEXT")
    private String deletionReason;

    @Column(columnDefinition = "TEXT")
    private String roles;
}