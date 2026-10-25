package com.example.WordGame.modules.badge.service;

import com.example.WordGame.modules.badge.Entities.Badge;
import com.example.WordGame.modules.badge.BadgeDTO.BadgeRequestDTO;
import com.example.WordGame.modules.badge.BadgeDTO.BadgeResponseDTO;
import com.example.WordGame.modules.badge.enums.BadgeRarity;
import com.example.WordGame.modules.badge.enums.BadgeType;
import com.example.WordGame.modules.badge.repository.BadgeRepository;
import com.example.WordGame.modules.genre.Entities.Genre;
import com.example.WordGame.modules.genre.repository.GenreRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BadgeServiceImplTest {

    @Mock
    private BadgeRepository badgeRepository;

    @Mock
    private GenreRepo genreRepository;

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

    @Test
    void createBadgeSupportsEveryBadgeType() {
        for (BadgeType badgeType : BadgeType.values()) {
            BadgeRequestDTO request = new BadgeRequestDTO();
            request.setBadgeName("Type " + badgeType);
            request.setDescription("Badge type coverage");
            request.setBadgeType(badgeType);
            request.setRarity(BadgeRarity.RARE);
            request.setDisplayOrder(1);

            when(badgeRepository.save(any(Badge.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            BadgeResponseDTO response = badgeService.createBadge(request);

            assertEquals(badgeType, response.getBadgeType());
        }
    }

    @Test
    void createBadgeCanBeAssignedToExistingGenre() {
        Genre genre = new Genre();
        genre.setName("Test1");
        genre.setId(42L);
        BadgeRequestDTO request = new BadgeRequestDTO();
        request.setBadgeName("Test1 Badge");
        request.setDescription("Genre-specific badge");
        request.setBadgeType(BadgeType.GENRE);
        request.setRarity(BadgeRarity.RARE);
        request.setDisplayOrder(1);
        request.setGenreId(genre.getId());

        when(genreRepository.findById(genre.getId())).thenReturn(java.util.Optional.of(genre));
        when(badgeRepository.save(any(Badge.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BadgeResponseDTO response = badgeService.createBadge(request);

        assertEquals(genre.getId(), response.getGenreId());
        assertEquals("Test1", response.getGenreName());
        verify(genreRepository).findById(genre.getId());
    }
}
