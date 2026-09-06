package com.example.WordGame.modules.roles.user.UserProfileDTO;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class DateOfBirthUpdateRequestDTO {
    @NotNull(message = "dateOfBirth is required")
    private LocalDate dateOfBirth;
}