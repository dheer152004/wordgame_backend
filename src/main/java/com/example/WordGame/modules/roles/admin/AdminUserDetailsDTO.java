package com.example.WordGame.modules.roles.admin;

import com.example.WordGame.modules.auth.Provider;
import com.example.WordGame.modules.roles.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserDetailsDTO {
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private Boolean isGuest;
    private Boolean isActive;
    private Boolean emailVerified;
    private LocalDateTime lastLogin;
    private LocalDateTime lastActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer totalXp;
    private Integer currentStreak;
    private Integer longestStreak;
    private Integer level;
    private Set<Role> roles;
    private Provider provider;
}