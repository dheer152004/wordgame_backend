package com.example.WordGame.modules.badge.service;

import com.example.WordGame.modules.badge.BadgeDTO.BadgeRequestDTO;
import com.example.WordGame.modules.badge.BadgeDTO.BadgeResponseDTO;
import com.example.WordGame.modules.badge.BadgeDTO.BadgeUpdateDTO;

import java.util.List;

public interface BadgeService {
    List<BadgeResponseDTO> getAllBadges();
    BadgeResponseDTO getBadgeById(Long id);
    BadgeResponseDTO createBadge(BadgeRequestDTO request);
    BadgeResponseDTO updateBadge(Long id, BadgeUpdateDTO request);
    void deleteBadge(Long id);
    int rebalanceDisplayOrder();
}
