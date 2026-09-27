package com.example.WordGame.modules.WOTD.dto;

import com.example.WordGame.modules.WOTD.enums.WordOfTheDayStatus;
import lombok.Data;

import java.time.LocalDate;

@Data
public class WordOfTheDayRequest {
    private Long wordId;
    private LocalDate publishOn;
    private WordOfTheDayStatus status;
}