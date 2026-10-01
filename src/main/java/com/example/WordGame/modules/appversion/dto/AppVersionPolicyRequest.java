package com.example.WordGame.modules.appversion.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record AppVersionPolicyRequest(
        @NotBlank String platform,
        @NotBlank @Pattern(regexp = "\\d+\\.\\d+\\.\\d+") String latestVersion,
        @NotBlank @Pattern(regexp = "\\d+\\.\\d+\\.\\d+") String minimumVersion,
        @NotNull Boolean forceUpdate,
        @NotBlank String title,
        @NotBlank String message,
        @NotBlank String buttonText,
        @NotBlank @Pattern(regexp = "https?://.+") String storeUrl) {
}