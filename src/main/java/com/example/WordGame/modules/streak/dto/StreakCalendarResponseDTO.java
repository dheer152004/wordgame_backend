package com.example.WordGame.modules.streak.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class StreakCalendarResponseDTO {
    private String month;
    private Integer currentStreak;
    private Integer longestStreak;
    private LocalDate lastActivityDate;
    private LocalDate trackedFromDate;
    private Boolean streakUpdatedToday;
    private List<LocalDate> activeDates;
}