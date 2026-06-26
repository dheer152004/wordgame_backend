package com.example.WordGame.modules.auth.service;

import com.example.WordGame.exceptions.ApiException;
import com.example.WordGame.modules.auth.DTO.LoginRequest;
import com.example.WordGame.modules.auth.DTO.LoginResponseDTO;
import com.example.WordGame.modules.auth.DTO.RegisterRequest;
import com.example.WordGame.modules.auth.repository.AuthUtil;
import com.example.WordGame.modules.roles.Role;
import com.example.WordGame.modules.roles.user.Entities.User;
import com.example.WordGame.modules.roles.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Map;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;
    private final PasswordEncoder passwordEncoder;
    private final TokenBlacklistService tokenBlacklistService;

    @Value("${app.admin.create-secret:}")
    private String adminCreateSecret;

    public LoginResponseDTO login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String token = authUtil.generateToken(user);

        // Get user from authenticated principal (handles login-by-email)
        User userInDB = userRepository.findByUsername(user.getUsername())
            .orElseThrow(() -> new ApiException("User not found"));

        // Update last login
        userInDB.setLastLogin(LocalDateTime.now());
        userInDB.setLastActive(LocalDateTime.now());
        userRepository.save(userInDB);

        return LoginResponseDTO.builder()
                .token(token)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                // .avatarUrl(user.getAvatarUrl())
                // .totalXp(user.getTotalXp())
                // .level(user.getLevel())
                // .currentStreak(user.getCurrentStreak())
                .roles(user.getRoles() == null ? java.util.List.of() : user.getRoles().stream().map(Enum::name).toList())
                .provider("email")
                .build();
    }

    public LoginResponseDTO loginWithRole(LoginRequest loginRequest, Role requiredRole) {
        LoginResponseDTO resp = login(loginRequest);
        if (resp.getRoles() == null || !resp.getRoles().contains(requiredRole.name())) {
            throw new ApiException("User does not have required role: " + requiredRole.name());
        }
        return resp;
    }

    public LoginResponseDTO register(RegisterRequest registerRequest) {
        // Trim inputs and log attempt
        String username = registerRequest.getUsername() != null ? registerRequest.getUsername().trim() : null;
        String email = registerRequest.getEmail() != null ? registerRequest.getEmail().trim() : null;
        log.info("Register attempt: username='{}' email='{}'", username, email);

        // Check if username or email exists
        boolean usernameExists = username != null && userRepository.existsByUsername(username);
        boolean emailExists = email != null && userRepository.existsByEmail(email);
        log.info("existsByUsername={} existsByEmail={}", usernameExists, emailExists);

        if (usernameExists) {
            throw new ApiException("Username already exists");
        }
        if (emailExists) {
            throw new ApiException("Email already exists");
        }
        // Create user
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setDisplayName(registerRequest.getDisplayName() != null ? registerRequest.getDisplayName() : registerRequest.getUsername());
        user.setAvatarUrl("https://ui-avatars.com/api/?background=random&name=" + registerRequest.getUsername());
        // Assign default role USER (use insertion-ordered set for predictable output)
        java.util.Set<Role> roles = new java.util.LinkedHashSet<>();
        roles.add(Role.USER);
        user.setRoles(roles);

        userRepository.save(user);

        return login(new LoginRequest(registerRequest.getUsername(), registerRequest.getPassword()));
    }

    public LoginResponseDTO registerAdmin(RegisterRequest registerRequest, String secret) {
        if (adminCreateSecret == null || adminCreateSecret.isBlank() || !adminCreateSecret.equals(secret)) {
            throw new ApiException("Invalid admin creation secret");
        }

        // reuse register flow but create admin role as well
        String username = registerRequest.getUsername() != null ? registerRequest.getUsername().trim() : null;
        String email = registerRequest.getEmail() != null ? registerRequest.getEmail().trim() : null;

        boolean usernameExists = username != null && userRepository.existsByUsername(username);
        boolean emailExists = email != null && userRepository.existsByEmail(email);

        if (usernameExists) {
            throw new ApiException("Username already exists");
        }
        if (emailExists) {
            throw new ApiException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setDisplayName(registerRequest.getDisplayName() != null ? registerRequest.getDisplayName() : registerRequest.getUsername());
        user.setAvatarUrl("https://ui-avatars.com/api/?background=random&name=" + registerRequest.getUsername());
        // Use insertion-ordered set so roles serialize as USER, EDITOR, ADMIN
        java.util.Set<Role> roles = new java.util.LinkedHashSet<>();
        roles.add(Role.USER);
        roles.add(Role.EDITOR);
        roles.add(Role.ADMIN);
        user.setRoles(roles);

        userRepository.save(user);

        return login(new LoginRequest(registerRequest.getUsername(), registerRequest.getPassword()));
    }

    public void logout(String bearerToken) {
        if (bearerToken == null) return;
        String token = bearerToken.startsWith("Bearer ") ? bearerToken.split("Bearer ")[1] : bearerToken;
        try {
            java.util.Date exp = authUtil.getExpirationDateFromToken(token);
            tokenBlacklistService.blacklistToken(token, exp);
        } catch (Exception ignored) {}
    }

    public LoginResponseDTO oauthLogin(String providerStr, String token) {
        if (providerStr == null || token == null) throw new ApiException("Provider and token required");
        com.example.WordGame.modules.auth.Provider provider;
        try {
            provider = com.example.WordGame.modules.auth.Provider.valueOf(providerStr.trim().toUpperCase());
        } catch (Exception ex) {
            throw new ApiException("Unsupported provider: " + providerStr);
        }

        Map<String, Object> profile = null;
        // For Google and Apple, prefer signature-verified id_token
        try {
            if (provider == com.example.WordGame.modules.auth.Provider.GOOGLE) {
                String jwks = "https://www.googleapis.com/oauth2/v3/certs";
                JwtVerifier verifier = new JwtVerifier(jwks);
                profile = verifier.verifyAndGetClaims(token);
            } else if (provider == com.example.WordGame.modules.auth.Provider.APPLE) {
                String jwks = "https://appleid.apple.com/auth/keys";
                JwtVerifier verifier = new JwtVerifier(jwks);
                profile = verifier.verifyAndGetClaims(token);
            } else {
                profile = fetchProfile(provider, token);
            }
        } catch (Exception ex) {
            log.warn("Signature verification failed or not applicable, falling back to HTTP verification: {}", ex.getMessage());
            profile = fetchProfile(provider, token);
        }
        if (profile == null || profile.get("email") == null) {
            throw new ApiException("Unable to verify token with provider: " + provider.name());
        }

        String email = (String) profile.get("email");
        String displayName = (String) profile.getOrDefault("name", (String) profile.getOrDefault("displayName", null));
        String avatar = (String) profile.getOrDefault("picture", null);

        // Find or create user by email
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            user = new User();
            // generate username from email prefix + provider
            String username = email.contains("@") ? email.substring(0, email.indexOf('@')) : email;
            username = username + "_" + provider.name().toLowerCase();
            // ensure uniqueness
            String base = username;
            int i = 1;
            while (userRepository.existsByUsername(username)) {
                username = base + i++;
            }
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            user.setDisplayName(displayName != null ? displayName : username);
            user.setAvatarUrl(avatar != null ? avatar : "https://ui-avatars.com/api/?background=random&name=" + username);
            user.setProvider(provider);
            java.util.Set<com.example.WordGame.modules.roles.Role> roles = new java.util.LinkedHashSet<>();
            roles.add(com.example.WordGame.modules.roles.Role.USER);
            user.setRoles(roles);
            userRepository.save(user);
        } else {
            // update profile info
            if (displayName != null) user.setDisplayName(displayName);
            if (avatar != null) user.setAvatarUrl(avatar);
            // persist provider if not already set or different
            if (user.getProvider() == null || user.getProvider() != provider) {
                user.setProvider(provider);
            }
            user.setLastActive(LocalDateTime.now());
            userRepository.save(user);
        }

        String jwt = authUtil.generateToken(user);

        return LoginResponseDTO.builder()
                .token(jwt)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
            .roles(user.getRoles() == null ? java.util.List.of() : user.getRoles().stream().map(Enum::name).toList())
            .provider(provider.name().toLowerCase())
                .build();
    }

    private Map<String, Object> fetchProfile(com.example.WordGame.modules.auth.Provider provider, String token) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            if (provider == com.example.WordGame.modules.auth.Provider.GOOGLE) {
                // Verify Google id_token via tokeninfo endpoint
                String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + java.net.URLEncoder.encode(token, StandardCharsets.UTF_8);
                HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                try (InputStream is = conn.getInputStream()) {
                    Map<String, Object> resp = mapper.readValue(is, Map.class);
                    // tokeninfo returns 'email', 'name', 'picture', 'sub'
                    return Map.of(
                            "email", resp.get("email"),
                            "name", resp.get("name"),
                            "picture", resp.get("picture"),
                            "id", resp.get("sub")
                    );
                }
            } else if (provider == com.example.WordGame.modules.auth.Provider.MICROSOFT) {
                // Use Microsoft Graph to fetch profile with access token
                URL url = new URL("https://graph.microsoft.com/v1.0/me");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Authorization", "Bearer " + token);
                conn.setRequestProperty("Accept", "application/json");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                try (InputStream is = conn.getInputStream()) {
                    Map<String, Object> resp = mapper.readValue(is, Map.class);
                    // resp contains id, displayName, userPrincipalName
                    return Map.of(
                            "email", resp.getOrDefault("mail", resp.getOrDefault("userPrincipalName", null)),
                            "name", resp.get("displayName"),
                            "id", resp.get("id")
                    );
                }
            } else if (provider == com.example.WordGame.modules.auth.Provider.APPLE) {
                // Apple gives id_token (JWT). Decode payload without signature verification to extract email/sub
                String[] parts = token.split("\\.");
                if (parts.length < 2) return null;
                byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
                String payload = new String(decoded, StandardCharsets.UTF_8);
                Map<String, Object> resp = mapper.readValue(payload, Map.class);
                return Map.of(
                        "email", resp.get("email"),
                        "name", resp.get("name"),
                        "id", resp.get("sub")
                );
            }
        } catch (ApiException ae) { throw ae; }
        catch (Exception ex) {
            log.error("OAuth profile fetch error", ex);
        }
        return null;
    }
}
