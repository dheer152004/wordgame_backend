package com.example.WordGame.DTO.User;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {
    @NotBlank(message = "username is required")
    private String username;

    @NotBlank(message = "Password is required")
    private String password;
}
