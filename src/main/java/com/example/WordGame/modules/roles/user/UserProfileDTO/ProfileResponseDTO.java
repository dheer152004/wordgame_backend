package com.example.WordGame.modules.roles.user.UserProfileDTO;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class ProfileResponseDTO {
    // Basic Info
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private String bio;
    private LocalDate  dateOfBirth;
    private String location;

    // Social Links
//    private String socialInstagram;
//    private String socialTwitter;
//    private String socialLinkedin;

    // Gamification Stats
    private Integer totalXp;
    private Integer level;
    private Integer currentStreak;
    private Integer longestStreak;
    private Integer xpToNextLevel;
    private Integer levelProgress;

    // Learning Stats
    private Long totalWordsSaved;
    private Integer totalQuizzesCompleted;
    private Double averageQuizScore;
    private Integer wordsMastered;

    // Activity
    private LocalDateTime lastActive;
    private LocalDateTime createdAt;
    private LocalDate lastQuizDate;

    // Badges/Achievements (Optional)
    private List<String> recentBadges;
}
