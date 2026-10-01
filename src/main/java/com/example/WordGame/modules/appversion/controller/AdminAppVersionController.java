package com.example.WordGame.modules.appversion.controller;

import com.example.WordGame.modules.appversion.dto.AdminAppVersionResponse;
import com.example.WordGame.modules.appversion.dto.AppVersionPolicyRequest;
import com.example.WordGame.modules.appversion.dto.AppVersionStatusRequest;
import com.example.WordGame.modules.appversion.service.AppVersionPolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(value = "/api/v1/admin/app-versions", produces = "application/json")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminAppVersionController {
    private final AppVersionPolicyService service;

    @PostMapping(consumes = "application/json")
    public ResponseEntity<AdminAppVersionResponse> create(@Valid @RequestBody AppVersionPolicyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @GetMapping
    public ResponseEntity<List<AdminAppVersionResponse>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{platform}")
    public ResponseEntity<AdminAppVersionResponse> findOne(@PathVariable("platform") String platform) {
        return ResponseEntity.ok(service.findOne(platform));
    }

    @PutMapping(path = "/{platform}", consumes = "application/json")
    public ResponseEntity<AdminAppVersionResponse> update(
            @PathVariable("platform") String platform, @Valid @RequestBody AppVersionPolicyRequest request) {
        return ResponseEntity.ok(service.update(platform, request));
    }

    @PatchMapping(path = "/{platform}/status", consumes = "application/json")
    public ResponseEntity<AdminAppVersionResponse> setStatus(
            @PathVariable("platform") String platform, @Valid @RequestBody AppVersionStatusRequest request) {
        return ResponseEntity.ok(service.setEnabled(platform, request.enabled()));
    }
}