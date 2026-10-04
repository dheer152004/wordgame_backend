package com.example.WordGame.modules.streak.service;

import com.example.WordGame.modules.roles.user.Entities.User;
import com.example.WordGame.modules.streak.repository.StreakActivityRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class StreakServiceTest {
    @Mock
    private StreakActivityRepository streakActivityRepository;

    @InjectMocks
    private StreakService streakService;

    @Test
    void firstActivityStartsAtOne() {
        User user = userWithStreak(0, 0, null);

        StreakService.StreakUpdate result = streakService.recordQuizCompletion(
                user, LocalDate.of(2026, 10, 2));

        assertEquals(1, result.currentStreak());
        assertEquals(1, result.longestStreak());
    }

    @Test
    void repeatActivityOnSameDateDoesNotIncrement() {
        User user = userWithStreak(4, 7, LocalDate.of(2026, 10, 2));

        StreakService.StreakUpdate result = streakService.recordQuizCompletion(
                user, LocalDate.of(2026, 10, 2));

        assertEquals(4, result.currentStreak());
        assertEquals(7, result.longestStreak());
        assertFalse(result.milestoneReached());
    }

    @Test
    void activityOnNextCalendarDayIncrementsOnce() {
        User user = userWithStreak(4, 7, LocalDate.of(2026, 10, 1));

        StreakService.StreakUpdate result = streakService.recordQuizCompletion(
                user, LocalDate.of(2026, 10, 2));

        assertEquals(5, result.currentStreak());
        assertEquals(7, result.longestStreak());
    }

    @Test
    void repeatedActivitiesForOneDayCountOnlyOnce() {
        User user = userWithStreak(3, 3, LocalDate.of(2026, 10, 1));

        for (int activity = 0; activity < 10; activity++) {
            streakService.recordQuizCompletion(user, LocalDate.of(2026, 10, 2));
        }

        assertEquals(4, user.getCurrentStreak());
        assertEquals(4, user.getLongestStreak());
        verify(streakActivityRepository, times(1)).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void gapOfSeveralDaysResetsCurrentStreak() {
        User user = userWithStreak(10, 10, LocalDate.of(2026, 10, 1));

        StreakService.StreakUpdate result = streakService.recordQuizCompletion(
                user, LocalDate.of(2026, 10, 4));

        assertEquals(1, result.currentStreak());
        assertEquals(10, result.longestStreak());
    }

    @Test
    void reachingNewHighUpdatesLongestStreakAndMilestone() {
        User user = userWithStreak(10, 10, LocalDate.of(2026, 10, 1));

        StreakService.StreakUpdate result = streakService.recordQuizCompletion(
                user, LocalDate.of(2026, 10, 2));

        assertEquals(11, result.currentStreak());
        assertEquals(11, result.longestStreak());
        assertFalse(result.milestoneReached());
    }

    @Test
    void milestoneIsReportedAtSevenDays() {
        User user = userWithStreak(6, 6, LocalDate.of(2026, 10, 1));

        StreakService.StreakUpdate result = streakService.recordQuizCompletion(
                user, LocalDate.of(2026, 10, 2));

        assertTrue(result.milestoneReached());
        assertEquals(7, result.streakMilestone());
    }

    @Test
    void activityDateUsesClientOffsetAtUtcMidnightBoundary() {
        Instant instant = Instant.parse("2026-10-02T00:30:00Z");

        assertEquals(LocalDate.of(2026, 10, 1),
                streakService.activityDate(instant, "-300"));
        assertEquals(LocalDate.of(2026, 10, 2),
                streakService.activityDate(instant, "330"));
    }

    private User userWithStreak(int current, int longest, LocalDate lastActivityDate) {
        User user = new User();
        user.setCurrentStreak(current);
        user.setLongestStreak(longest);
        user.setLastQuizDate(lastActivityDate);
        return user;
    }
}