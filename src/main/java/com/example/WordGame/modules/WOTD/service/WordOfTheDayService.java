package com.example.WordGame.modules.WOTD.service;

import com.example.WordGame.modules.WOTD.dto.WordOfTheDayRequest;
import com.example.WordGame.modules.WOTD.dto.WordOfTheDayPublicResponse;
import com.example.WordGame.modules.WOTD.dto.WordOfTheDayResponse;

import java.time.LocalDate;
import java.util.List;

public interface WordOfTheDayService {
    WordOfTheDayPublicResponse getToday();
    WordOfTheDayResponse create(WordOfTheDayRequest request);
    WordOfTheDayResponse update(Long id, WordOfTheDayRequest request);
    void delete(Long id);
    List<WordOfTheDayResponse> findBetween(LocalDate from, LocalDate to);
}