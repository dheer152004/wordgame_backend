package com.example.WordGame.modules.roles.admin;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DeletedUserDTO {
    private Long id;
    private Long originalUserId;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private Boolean isGuest;
    private Boolean emailVerified;
    private LocalDateTime lastLogin;
    private LocalDateTime lastActive;
    private LocalDateTime createdAt;
    private LocalDateTime deletedAt;
    private String deletionReason;
    private List<String> roles;
}