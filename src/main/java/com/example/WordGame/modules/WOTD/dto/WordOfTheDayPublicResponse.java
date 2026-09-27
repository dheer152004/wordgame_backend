package com.example.WordGame.modules.WOTD.dto;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class WordOfTheDayPublicResponse {
    boolean success;
    WordOfTheDayWordResponse wordOfTheDay;
}