package com.example.WordGame.modules.WOTD.service;

import com.example.WordGame.modules.WOTD.entity.WordOfTheDay;
import com.example.WordGame.modules.WOTD.enums.WordOfTheDayStatus;
import com.example.WordGame.modules.WOTD.repository.WordOfTheDayRepository;
import com.example.WordGame.modules.words.Entities.Word;
import com.example.WordGame.modules.words.repository.WordRepo;
import com.example.WordGame.modules.words.service.WordService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WordOfTheDayServiceImplTest {
    @Mock
    private WordOfTheDayRepository repository;

    @Mock
    private WordRepo wordRepository;

    @Mock
    private WordService wordService;

    @InjectMocks
    private WordOfTheDayServiceImpl service;

    @Test
    void shouldPublishScheduledWordsWhosePublishDateHasPassed() {
        WordOfTheDay missedEntry = new WordOfTheDay();
        missedEntry.setStatus(WordOfTheDayStatus.SCHEDULED);
        when(repository.findByPublishOnLessThanEqualAndStatus(any(LocalDate.class),
                eq(WordOfTheDayStatus.SCHEDULED))).thenReturn(List.of(missedEntry));

        service.publishDueWords();

        assertEquals(WordOfTheDayStatus.PUBLISHED, missedEntry.getStatus());
        verify(repository).saveAll(List.of(missedEntry));
    }

    @Test
    void shouldIncludeWordIdInAdminRangeResponse() {
        Word word = new Word();
        word.setId(42L);
        WordOfTheDay entry = new WordOfTheDay();
        entry.setId(3L);
        entry.setWord(word);
        entry.setPublishOn(LocalDate.of(2026, 9, 24));
        entry.setStatus(WordOfTheDayStatus.PUBLISHED);
        when(repository.findByPublishOnBetweenOrderByPublishOnAsc(
                LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 31)))
                .thenReturn(List.of(entry));

        var response = service.findBetween(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 10, 31));

        assertEquals(3L, response.get(0).getWordOfTheDayId());
        assertEquals(42L, response.get(0).getWordId());
    }
}