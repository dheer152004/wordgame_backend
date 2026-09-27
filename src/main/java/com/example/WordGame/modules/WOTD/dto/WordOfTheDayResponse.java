package com.example.WordGame.modules.WOTD.dto;

import com.example.WordGame.modules.WOTD.enums.WordOfTheDayStatus;
import lombok.Builder;
import lombok.Value;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Value
@Builder
public class WordOfTheDayResponse {
    Long id;
    Long wordOfTheDayId;
    // Long wordId;
    LocalDate publishOn;
    WordOfTheDayStatus status;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
}