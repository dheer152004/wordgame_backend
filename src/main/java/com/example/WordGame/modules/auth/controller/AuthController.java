package com.example.WordGame.modules.auth.controller;

import com.example.WordGame.modules.auth.DTO.LoginRequest;
import com.example.WordGame.modules.auth.DTO.LoginResponseDTO;
import com.example.WordGame.modules.auth.DTO.RegisterRequest;
import com.example.WordGame.modules.roles.Role;
import com.example.WordGame.modules.auth.service.AuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import com.example.WordGame.modules.roles.user.Entities.User;
import com.example.WordGame.modules.auth.DTO.OAuthRequest;

@RestController
@RequestMapping(value = "/api/auth", produces = "application/json")
@CrossOrigin("*")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @PostMapping("/login-admin")
    public ResponseEntity<LoginResponseDTO> loginAdmin(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.loginWithRole(loginRequest, Role.ADMIN));
    }

    @PostMapping("/login-editor")
    public ResponseEntity<LoginResponseDTO> loginEditor(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.loginWithRole(loginRequest, Role.EDITOR));
    }

    @PostMapping("/login-user")
    public ResponseEntity<LoginResponseDTO> loginUser(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.loginWithRole(loginRequest, Role.USER));
    }

    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(@Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.register(registerRequest));
    }

    @PostMapping("/register-admin")
    public ResponseEntity<LoginResponseDTO> registerAdmin(
            @RequestHeader(value = "X-Admin-Secret") String secret,
            @Valid @RequestBody RegisterRequest registerRequest) {
        return ResponseEntity.ok(authService.registerAdmin(registerRequest, secret));
    }

    @PostMapping("/logout")
    public ResponseEntity<java.util.Map<String, Object>> logout(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.logout(authorization);
        java.util.Map<String, Object> resp = new java.util.HashMap<>();
        resp.put("message", "Logged out");
        resp.put("success", true);
        return ResponseEntity.ok(resp);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal User user) {
        if (user == null) {
            return ResponseEntity.status(401).body(java.util.Map.of("authenticated", false));
        }
        return ResponseEntity.ok(java.util.Map.of(
                "authenticated", true,
                "id", user.getId(),
                "username", user.getUsername(),
                "roles", user.getRoles() == null ? java.util.List.of() : user.getRoles().stream().map(Enum::name).toList()
        ));
    }

    @PostMapping("/oauth")
    public ResponseEntity<LoginResponseDTO> oauth(@RequestBody OAuthRequest req) {
        return ResponseEntity.ok(authService.oauthLogin(req.getProvider(), req.getToken()));
    }

}
