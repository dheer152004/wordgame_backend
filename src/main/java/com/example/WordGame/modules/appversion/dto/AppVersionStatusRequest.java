package com.example.WordGame.modules.appversion.dto;

import jakarta.validation.constraints.NotNull;

public record AppVersionStatusRequest(@NotNull Boolean enabled) {
}