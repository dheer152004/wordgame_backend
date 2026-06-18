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
import java.util.Set;
import com.example.WordGame.modules.roles.Role;

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
}
