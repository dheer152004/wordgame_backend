package com.example.WordGame.modules.auth.controller;

import com.example.WordGame.modules.auth.DTO.LoginRequest;
import com.example.WordGame.modules.auth.DTO.LoginResponseDTO;
import com.example.WordGame.modules.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class LegacyAuthController {

    private final AuthService authService;

    // POST alias so frontends still targeting /api/v1/login work
    @PostMapping(value = "/api/v1/login", produces = "application/json")
    public ResponseEntity<LoginResponseDTO> legacyLogin(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    // Simple informational GET so browsers hitting the path don't get a 500/403 XML response
    @GetMapping(value = "/api/v1/login")
    public ResponseEntity<String> legacyLoginInfo() {
        String html = "<html><head><title>Login</title><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\">" +
            "<style>body{font-family:Arial,Helvetica,sans-serif;padding:24px} .btn{display:inline-block;margin:8px;padding:12px 18px;border-radius:6px;text-decoration:none;color:#fff} .google{background:#db4437} .github{background:#333} .info{margin-top:18px;color:#444}</style></head><body>" +
            "<h1>Login</h1>" +
            "<p class=\"info\">You can sign in using username/password (POST /api/v1/login) or use an OAuth provider:</p>" +
            "<a class=\"btn google\" href=\"/oauth2/authorization/google\">Continue with Google</a>" +
            "<a class=\"btn github\" href=\"/oauth2/authorization/github\">Continue with GitHub</a>" +
            "<p class=\"info\">If those providers are not configured, the links may return 404.</p>" +
            "</body></html>";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_HTML);
        return ResponseEntity.ok().headers(headers).body(html);
    }
}
