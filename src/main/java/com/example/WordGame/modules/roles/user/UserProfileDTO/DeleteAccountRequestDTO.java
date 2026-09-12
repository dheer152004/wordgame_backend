package com.example.WordGame.modules.roles.user.UserProfileDTO;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DeleteAccountRequestDTO {

    @Size(max = 1000, message = "Reason must not exceed 1000 characters")
    private String reason;
}