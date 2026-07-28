package com.example.WordGame.modules.badge.service;

import com.example.WordGame.modules.badge.Entities.Badge;
import com.example.WordGame.modules.badge.BadgeDTO.BadgeRequestDTO;
import com.example.WordGame.modules.badge.BadgeDTO.BadgeResponseDTO;
import com.example.WordGame.modules.badge.enums.BadgeRarity;
import com.example.WordGame.modules.badge.enums.BadgeType;
import com.example.WordGame.modules.badge.repository.BadgeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BadgeServiceImplTest {

    @Mock
    private BadgeRepository badgeRepository;

    @InjectMocks
    private BadgeServiceImpl badgeService;

    @Test
    void createBadgeShouldPersistAndMapFields() {
        BadgeRequestDTO request = new BadgeRequestDTO();
        request.setBadgeName("First Quiz");
        request.setDescription("Complete your first quiz");
        request.setBadgeType(BadgeType.QUIZ);
        request.setRarity(BadgeRarity.RARE);
        request.setHidden(true);
        request.setActive(true);
        request.setDisplayOrder(1);

        Badge badge = new Badge();
        badge.setId(100L);
        badge.setBadgeName("First Quiz");
        badge.setDescription("Complete your first quiz");
        badge.setBadgeType(BadgeType.QUIZ);
        badge.setRarity(BadgeRarity.RARE);
        badge.setHidden(true);
        badge.setActive(true);
        badge.setDisplayOrder(1);
        badge.setCreatedAt(LocalDateTime.now());
        badge.setUpdatedAt(LocalDateTime.now());

        when(badgeRepository.save(any(Badge.class))).thenReturn(badge);

        BadgeResponseDTO response = badgeService.createBadge(request);

        assertEquals(100L, response.getBadgeId());
        assertEquals("First Quiz", response.getBadgeName());
        assertEquals(BadgeType.QUIZ, response.getBadgeType());
        assertEquals(BadgeRarity.RARE, response.getRarity());
        assertEquals(true, response.getIsHidden());
        assertEquals(true, response.getIsActive());
        assertEquals(1, response.getDisplayOrder());
    }
}
