package com.example.WordGame.modules.auth.repository;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.WordGame.modules.roles.user.Entities.User;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;


@Component
public class AuthUtil {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Value("${app.jwt.access-expiration-ms:3600000}")
    private long accessExpirationMs;

    @Value("${app.jwt.refresh-expiration-ms:10368000000}")
    private long refreshExpirationMs;

    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(User user, UUID sessionId) {
        Objects.requireNonNull(sessionId, "sessionId");
        var builder = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId().toString())
                .claim("type", "access")
                .claim("sessionId", sessionId.toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + accessExpirationMs));
        return builder
                .signWith(getSecretKey())
                .compact();
    }

    public String generateRefreshToken(User user, UUID sessionId) {
        Objects.requireNonNull(sessionId, "sessionId");
        var builder = Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId().toString())
                .claim("type", "refresh")
                .claim("sessionId", sessionId.toString())
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationMs));
        return builder
                .signWith(getSecretKey())
                .compact();
    }

    public boolean isRefreshToken(String token) {
        return "refresh".equals(getTokenType(token));
    }

    public boolean isAccessToken(String token) {
        return "access".equals(getTokenType(token));
    }

    private String getTokenType(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("type", String.class);
    }

    public String getUsernameFromToken(String token) {

        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();

        return claims.getSubject();
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getExpiration();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = parseClaims(token);
        return Long.valueOf(claims.get("userId", String.class));
    }

    public UUID getSessionIdFromToken(String token) {
        String sessionId = parseClaims(token).get("sessionId", String.class);
        return sessionId == null ? null : UUID.fromString(sessionId);
    }

    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSecretKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
