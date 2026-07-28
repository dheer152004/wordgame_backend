package com.example.WordGame.modules.badge.service;

import com.example.WordGame.Service.ImageStorageService;
import com.example.WordGame.exceptions.ApiException;
import com.example.WordGame.modules.badge.BadgeDTO.BadgeRequestDTO;
import com.example.WordGame.modules.badge.BadgeDTO.BadgeResponseDTO;
import com.example.WordGame.modules.badge.BadgeDTO.BadgeUpdateDTO;
import com.example.WordGame.modules.badge.Entities.Badge;
import com.example.WordGame.modules.badge.repository.BadgeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class BadgeServiceImpl implements BadgeService {

    private final BadgeRepository badgeRepository;
    private final ImageStorageService imageUploadService;
    private static final int DISPLAY_ORDER_INITIAL = 10000;
    private static final int DISPLAY_ORDER_INCREMENT = 10000;

    @Override
    public List<BadgeResponseDTO> getAllBadges() {
        return badgeRepository.findAll().stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public BadgeResponseDTO getBadgeById(Long id) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new ApiException("Badge not found with id: " + id));
        return toResponseDto(badge);
    }

    @Override
    @Transactional
    public BadgeResponseDTO createBadge(BadgeRequestDTO request) {
        Badge badge = new Badge();
        badge.setBadgeName(request.getBadgeName());
        badge.setDescription(request.getDescription());
        badge.setIconUrl(resolveIconUrl(request.getIconUrl(), request.getImage()));
        badge.setBadgeType(request.getBadgeType());
        badge.setRarity(request.getRarity());
        badge.setHidden(request.getHidden() != null ? request.getHidden() : false);
        badge.setActive(request.getActive() != null ? request.getActive() : true);
        Integer requestedOrder = request.getDisplayOrder();
        if (requestedOrder != null) {
            badge.setDisplayOrder(requestedOrder);
        } else {
            Integer maxOrder = badgeRepository.findAll().stream()
                    .map(Badge::getDisplayOrder)
                    .filter(java.util.Objects::nonNull)
                    .max(Integer::compareTo)
                    .orElse(0);
            int nextOrder = maxOrder != null && maxOrder >= DISPLAY_ORDER_INITIAL
                    ? maxOrder + DISPLAY_ORDER_INCREMENT
                    : DISPLAY_ORDER_INITIAL;
            badge.setDisplayOrder(nextOrder);
        }

        return toResponseDto(badgeRepository.save(badge));
    }

    @Override
    @Transactional
    public BadgeResponseDTO updateBadge(Long id, BadgeUpdateDTO request) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new ApiException("Badge not found with id: " + id));

        if (request.getBadgeName() != null) {
            badge.setBadgeName(request.getBadgeName());
        }
        if (request.getDescription() != null) {
            badge.setDescription(request.getDescription());
        }
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            badge.setIconUrl(resolveIconUrl(null, request.getImage()));
        } else if (request.getIconUrl() != null) {
            badge.setIconUrl(request.getIconUrl());
        }
        if (request.getBadgeType() != null) {
            badge.setBadgeType(request.getBadgeType());
        }
        if (request.getRarity() != null) {
            badge.setRarity(request.getRarity());
        }
        if (request.getHidden() != null) {
            badge.setHidden(request.getHidden());
        }
        if (request.getActive() != null) {
            badge.setActive(request.getActive());
        }
        if (request.getDisplayOrder() != null) {
            badge.setDisplayOrder(request.getDisplayOrder());
        }

        return toResponseDto(badgeRepository.save(badge));
    }

    @Override
    @Transactional
    public void deleteBadge(Long id) {
        Badge badge = badgeRepository.findById(id)
                .orElseThrow(() -> new ApiException("Badge not found with id: " + id));
        badgeRepository.delete(badge);
    }

    @Override
    @Transactional
    public int rebalanceDisplayOrder() {
        List<Badge> badges = badgeRepository.findAll();
        if (badges == null || badges.isEmpty()) {
            return 0;
        }

        badges.sort(Comparator
                .comparing((Badge b) -> Optional.ofNullable(b.getDisplayOrder()).orElse(Integer.MAX_VALUE))
                .thenComparing(Badge::getId));

        int nextOrder = DISPLAY_ORDER_INITIAL;
        List<Badge> updatedBadges = new ArrayList<>();

        for (Badge badge : badges) {
            badge.setDisplayOrder(nextOrder);
            badge.setUpdatedAt(java.time.LocalDateTime.now());
            updatedBadges.add(badge);
            nextOrder += DISPLAY_ORDER_INCREMENT;
        }

        if (!updatedBadges.isEmpty()) {
            badgeRepository.saveAll(updatedBadges);
            return updatedBadges.size();
        }

        return 0;
    }

    private String resolveIconUrl(String iconUrl, org.springframework.web.multipart.MultipartFile image) {
        if (image != null && !image.isEmpty()) {
            try {
                return imageUploadService.uploadImage(image, "badges");
            } catch (Exception e) {
                throw new ApiException("Failed to upload badge image: " + e.getMessage());
            }
        }
        return iconUrl;
    }

    private BadgeResponseDTO toResponseDto(Badge badge) {
        BadgeResponseDTO response = new BadgeResponseDTO();
        response.setBadgeId(badge.getId());
        response.setBadgeName(badge.getBadgeName());
        response.setDescription(badge.getDescription());
        response.setIconUrl(badge.getIconUrl());
        response.setBadgeType(badge.getBadgeType());
        response.setRarity(badge.getRarity());
        response.setIsHidden(badge.getHidden());
        response.setIsActive(badge.getActive());
        response.setDisplayOrder(badge.getDisplayOrder());
        response.setCreatedAt(badge.getCreatedAt());
        response.setUpdatedAt(badge.getUpdatedAt());
        return response;
    }
}
