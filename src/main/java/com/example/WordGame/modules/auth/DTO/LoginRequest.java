package com.example.WordGame.modules.auth.DTO;

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

    private String deviceId;
    private String deviceName;

    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }
}
