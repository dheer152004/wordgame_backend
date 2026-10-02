package com.example.WordGame.modules.auth.service;

import java.util.UUID;

public record IssuedAuthTokens(String accessToken, String refreshToken, UUID sessionId) {
}