package com.example.WordGame.modules.auth.controller;

import com.example.WordGame.modules.auth.DTO.LoginRequest;
import com.example.WordGame.modules.auth.DTO.LoginResponseDTO;
import com.example.WordGame.modules.auth.DTO.RegisterRequest;
import com.example.WordGame.modules.roles.Role;
import com.example.WordGame.modules.auth.service.AuthService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
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

    @GetMapping("/verify-email")
    public ResponseEntity<Map<String, Object>> verifyEmail(@RequestParam("token") String token) {
        boolean verified = authService.verifyEmail(token);
        return ResponseEntity.ok(Map.of(
                "success", verified,
                "message", verified ? "Email verified successfully" : "Email verification failed"
        ));
    }

            @GetMapping(value = "/verify-email", produces = MediaType.TEXT_HTML_VALUE)
            public ResponseEntity<String> verifyEmailPage(@RequestParam("token") String token) {
            boolean verified = authService.verifyEmail(token);
            String title = verified ? "Email verified" : "Verification failed";
            String heading = verified ? "Your email is verified" : "We could not verify your email";
            String message = verified
                ? "Your WordGame account is ready. You can close this page and sign in."
                : "This verification link may be invalid, expired, or already used. Please request a new link.";
            String statusClass = verified ? "success" : "error";
            String icon = verified ? "&#10003;" : "!";

            String html = "<!doctype html>"
                + "<html lang=\"en\"><head>"
                + "<meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">"
                + "<title>" + title + " | WordGame</title>"
                + "<style>"
                + "*{box-sizing:border-box}body{margin:0;min-height:100vh;display:grid;place-items:center;"
                + "font-family:Georgia,'Times New Roman',serif;background:#f3efe7;color:#20251f;padding:24px}"
                + ".panel{width:min(100%,560px);background:#fffdf8;border:1px solid #d8d0c2;"
                + "box-shadow:0 18px 50px rgba(49,43,32,.12);padding:48px 40px;text-align:center}"
                + ".mark{width:76px;height:76px;margin:0 auto 26px;border-radius:50%;display:grid;place-items:center;"
                + "font:700 38px Arial,sans-serif}.success .mark{background:#dcefe2;color:#1d7545}.error .mark{background:#f8e3dc;color:#a53b2d}"
                + "h1{font-size:clamp(28px,6vw,42px);font-weight:500;line-height:1.1;margin:0 0 14px}"
                + "p{font:16px/1.6 Arial,sans-serif;color:#657066;margin:0 auto;max-width:420px}.brand{"
                + "font:700 12px/1 Arial,sans-serif;letter-spacing:2px;text-transform:uppercase;color:#758878;margin-bottom:28px}"
                + "@media(max-width:480px){.panel{padding:38px 24px}}"
                + "</style></head><body><main class=\"panel " + statusClass + "\">"
                + "<div class=\"brand\">WordGame</div><div class=\"mark\">" + icon + "</div>"
                + "<h1>" + heading + "</h1><p>" + message + "</p>"
                + "</main></body></html>";

            return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
            }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, Object>> forgotPassword(@RequestBody Map<String, String> request) {
        String email = request != null ? request.get("email") : null;
        return ResponseEntity.ok(authService.forgotPassword(email));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, Object>> resetPassword(@RequestBody Map<String, String> request) {
        String token = request != null ? request.get("token") : null;
        String newPassword = request != null ? request.get("newPassword") : null;
        return ResponseEntity.ok(authService.resetPassword(token, newPassword));
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<Map<String, Object>> resendVerification(@RequestBody Map<String, String> request) {
        String email = request != null ? request.get("email") : null;
        return ResponseEntity.ok(authService.resendVerificationEmail(email));
    }

}
