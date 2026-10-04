package com.example.WordGame.modules.streak.service;

import com.example.WordGame.exceptions.ApiException;
import com.example.WordGame.modules.roles.user.Entities.User;
import com.example.WordGame.modules.streak.dto.StreakCalendarResponseDTO;
import com.example.WordGame.modules.streak.entity.StreakActivity;
import com.example.WordGame.modules.streak.repository.StreakActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StreakService {
    private static final Set<Integer> MILESTONES = Set.of(7, 14, 30, 50, 100, 365);
    private final StreakActivityRepository streakActivityRepository;

    public LocalDate activityDate(String timezoneOffsetMinutes) {
        return activityDate(Instant.now(), parseOffset(timezoneOffsetMinutes));
    }

    LocalDate activityDate(Instant instant, String timezoneOffsetMinutes) {
        return activityDate(instant, parseOffset(timezoneOffsetMinutes));
    }

    private LocalDate activityDate(Instant instant, ZoneOffset offset) {
        return instant.atOffset(offset).toLocalDate();
    }

    public boolean isActivityToday(LocalDate lastActivityDate, String timezoneOffsetMinutes) {
        return lastActivityDate != null && lastActivityDate.equals(activityDate(timezoneOffsetMinutes));
    }

    public StreakUpdate recordQuizCompletion(User user, LocalDate activityDate) {
        LocalDate lastActivityDate = user.getLastQuizDate();
        int currentStreak = valueOrZero(user.getCurrentStreak());
        int longestStreak = valueOrZero(user.getLongestStreak());

        if (!activityDate.equals(lastActivityDate)
            && !streakActivityRepository.existsByUserAndActivityDate(user, activityDate)) {
            streakActivityRepository.save(new StreakActivity(user, activityDate));
        }

        if (activityDate.equals(lastActivityDate)) {
            return new StreakUpdate(
                    currentStreak, longestStreak, lastActivityDate, true, false, null);
        }

        int updatedStreak = lastActivityDate == null
                ? 1
                : lastActivityDate.plusDays(1).equals(activityDate) ? currentStreak + 1 : 1;
        int updatedLongestStreak = Math.max(longestStreak, updatedStreak);
        Integer milestone = isMilestone(updatedStreak) ? updatedStreak : null;

        user.setCurrentStreak(updatedStreak);
        user.setLongestStreak(updatedLongestStreak);
        user.setLastQuizDate(activityDate);

        return new StreakUpdate(
                updatedStreak, updatedLongestStreak, activityDate, true, milestone != null, milestone);
    }

    public boolean isMilestone(int streak) {
        return MILESTONES.contains(streak);
    }

    public StreakCalendarResponseDTO getCalendar(User user, YearMonth month, String timezoneOffsetMinutes) {
        LocalDate startDate = month.atDay(1);
        LocalDate endDate = month.atEndOfMonth();
        List<LocalDate> activeDates = streakActivityRepository
                .findByUserAndActivityDateBetweenOrderByActivityDateAsc(user, startDate, endDate)
                .stream()
                .map(StreakActivity::getActivityDate)
                .collect(Collectors.toList());
        if (user.getLastQuizDate() != null
                && !activeDates.contains(user.getLastQuizDate())
                && !user.getLastQuizDate().isBefore(startDate)
                && !user.getLastQuizDate().isAfter(endDate)) {
            activeDates.add(user.getLastQuizDate());
            activeDates.sort(LocalDate::compareTo);
        }

        LocalDate trackedFromDate = streakActivityRepository.findFirstByUserOrderByActivityDateAsc(user)
                .map(StreakActivity::getActivityDate)
                .orElse(user.getLastQuizDate());
        if (user.getLastQuizDate() != null
                && (trackedFromDate == null || user.getLastQuizDate().isBefore(trackedFromDate))) {
            trackedFromDate = user.getLastQuizDate();
        }

        return StreakCalendarResponseDTO.builder()
                .month(month.toString())
                .currentStreak(valueOrZero(user.getCurrentStreak()))
                .longestStreak(valueOrZero(user.getLongestStreak()))
                .lastActivityDate(user.getLastQuizDate())
                .trackedFromDate(trackedFromDate)
                .streakUpdatedToday(isActivityToday(user.getLastQuizDate(), timezoneOffsetMinutes))
                .activeDates(activeDates)
                .build();
    }

    private ZoneOffset parseOffset(String timezoneOffsetMinutes) {
        if (timezoneOffsetMinutes == null || timezoneOffsetMinutes.isBlank()) {
            return ZoneOffset.UTC;
        }

        try {
            int minutes = Integer.parseInt(timezoneOffsetMinutes);
            if (minutes < -18 * 60 || minutes > 18 * 60) {
                throw new NumberFormatException("Offset outside supported range");
            }
            return ZoneOffset.ofTotalSeconds(minutes * 60);
        } catch (RuntimeException exception) {
            throw new ApiException("Invalid X-User-Timezone-Offset-Minutes header");
        }
    }

    private int valueOrZero(Integer value) {
        return value == null ? 0 : value;
    }

    public record StreakUpdate(
            int currentStreak,
            int longestStreak,
            LocalDate lastActivityDate,
            boolean streakUpdatedToday,
            boolean milestoneReached,
            Integer streakMilestone) {
    }
}