package com.example.WordGame.modules.appversion.dto;

public record AdminAppVersionResponse(
        String platform,
        String latestVersion,
        String minimumVersion,
        boolean forceUpdate,
        String title,
        String message,
        String buttonText,
        String storeUrl,
        boolean enabled) {
}