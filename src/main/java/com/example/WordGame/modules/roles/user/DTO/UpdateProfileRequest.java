package com.example.WordGame.modules.roles.user.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateProfileRequest {
    private String displayName;
    private String avatarUrl;
    private String bio;
}
