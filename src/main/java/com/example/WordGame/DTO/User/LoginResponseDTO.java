package com.example.WordGame.DTO.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoginResponseDTO {
    private String token;
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String avatarUrl;
    private Integer totalXp;
    private Integer level;
    private Integer currentStreak;
}
