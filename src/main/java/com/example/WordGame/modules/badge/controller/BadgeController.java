package com.example.WordGame.modules.badge.controller;

import com.example.WordGame.modules.badge.BadgeDTO.BadgeRequestDTO;
import com.example.WordGame.modules.badge.BadgeDTO.BadgeResponseDTO;
import com.example.WordGame.modules.badge.BadgeDTO.BadgeUpdateDTO;
import com.example.WordGame.modules.badge.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/badges", produces = "application/json")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BadgeController {

    private final BadgeService badgeService;

    @GetMapping
    public ResponseEntity<List<BadgeResponseDTO>> getAllBadges() {
        return ResponseEntity.ok(badgeService.getAllBadges());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BadgeResponseDTO> getBadgeById(@PathVariable Long id) {
        return ResponseEntity.ok(badgeService.getBadgeById(id));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BadgeResponseDTO> createBadge(@RequestBody BadgeRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(badgeService.createBadge(request));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BadgeResponseDTO> createBadgeMultipart(@RequestPart(value = "badge", required = false) BadgeRequestDTO request,
                                                                 @RequestPart(value = "image", required = false) MultipartFile image) {
        if (request == null) {
            request = new BadgeRequestDTO();
        }
        if (image != null && !image.isEmpty()) {
            request.setImage(image);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(badgeService.createBadge(request));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<BadgeResponseDTO> updateBadge(@PathVariable Long id, @RequestBody BadgeUpdateDTO request) {
        return ResponseEntity.ok(badgeService.updateBadge(id, request));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BadgeResponseDTO> updateBadgeMultipart(@PathVariable Long id,
                                                                 @RequestPart(value = "badge", required = false) BadgeUpdateDTO request,
                                                                 @RequestPart(value = "image", required = false) MultipartFile image) {
        if (request == null) {
            request = new BadgeUpdateDTO();
        }
        if (image != null && !image.isEmpty()) {
            request.setImage(image);
        }
        return ResponseEntity.ok(badgeService.updateBadge(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteBadge(@PathVariable Long id) {
        badgeService.deleteBadge(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Badge deleted successfully");
        return ResponseEntity.ok(response);
    }
}
