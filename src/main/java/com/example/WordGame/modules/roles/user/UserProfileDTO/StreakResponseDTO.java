package com.example.WordGame.modules.roles.user.UserProfileDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StreakResponseDTO {
    private Integer currentStreak;
    private Integer longestStreak;
}
