package com.example.WordGame.modules.badge.controller;

import com.example.WordGame.modules.badge.service.BadgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/admin/badges", produces = "application/json")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminBadgeController {

    private final BadgeService badgeService;

    @PostMapping("/rebalance-display-order")
    public ResponseEntity<Map<String, Object>> rebalanceDisplayOrder() {
        int updatedRecords = badgeService.rebalanceDisplayOrder();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("updatedRecords", updatedRecords);
        response.put("message", updatedRecords == 0 ? "No badges found." : "Display order rebalanced successfully.");
        return ResponseEntity.ok(response);
    }
}
