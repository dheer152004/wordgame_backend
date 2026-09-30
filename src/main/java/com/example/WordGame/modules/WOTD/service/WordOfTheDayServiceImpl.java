package com.example.WordGame.modules.WOTD.service;

import com.example.WordGame.exceptions.ApiException;
import com.example.WordGame.modules.WOTD.dto.WordOfTheDayPublicResponse;
import com.example.WordGame.modules.WOTD.dto.WordOfTheDayRequest;
import com.example.WordGame.modules.WOTD.dto.WordOfTheDayResponse;
import com.example.WordGame.modules.WOTD.dto.WordOfTheDayWordResponse;
import com.example.WordGame.modules.WOTD.entity.WordOfTheDay;
import com.example.WordGame.modules.WOTD.enums.WordOfTheDayStatus;
import com.example.WordGame.modules.WOTD.repository.WordOfTheDayRepository;
import com.example.WordGame.modules.words.DTO.WordDetailResponseDTO;
import com.example.WordGame.modules.words.Entities.Word;
import com.example.WordGame.modules.words.repository.WordRepo;
import com.example.WordGame.modules.words.service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class WordOfTheDayServiceImpl implements WordOfTheDayService {
    private final WordOfTheDayRepository repository;
    private final WordRepo wordRepository;
    private final WordService wordService;

    @Override
    @Transactional
    public WordOfTheDayPublicResponse getToday() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Kolkata"));
        publishDueWords(today);
        WordOfTheDay entry = repository.findByPublishOnAndStatus(today, WordOfTheDayStatus.PUBLISHED)
            .orElseThrow(() -> new ApiException("No word of the day is published today"));
        Word word = entry.getWord();
        WordDetailResponseDTO details = wordService.getWordDetail(word.getId());
        String wordType = word.getWordType() == null ? null
            : word.getWordType() == com.example.WordGame.modules.words.WordType.NORMAL_WORD
            ? "WORD" : word.getWordType().name();
        WordOfTheDayWordResponse wordResponse = WordOfTheDayWordResponse.builder()
            .wordId(word.getId())
            .word(details.getWord())
            .wordType(wordType)
            .partOfSpeech(word.getPartOfSpeech() == null ? null : word.getPartOfSpeech().name())
            .meaning(details.getMeaning())
            .description(details.getDescription())
            .images(details.getImages())
            .videos(details.getVideos())
            .audios(details.getAudios())
            .facts(details.getFacts())
            .examples(details.getExamples())
            .categories(details.getCategories())
            .build();
        return WordOfTheDayPublicResponse.builder()
            .success(true)
            .wordOfTheDay(wordResponse)
            .build();
    }

    @Override
    @Transactional
    public WordOfTheDayResponse create(WordOfTheDayRequest request) {
        requireCreateFields(request);
        ensureDateIsAvailable(request.getPublishOn(), null);
        WordOfTheDay entry = new WordOfTheDay();
        entry.setWord(findWord(request.getWordId()));
        entry.setPublishOn(request.getPublishOn());
        entry.setStatus(request.getStatus() == null ? WordOfTheDayStatus.SCHEDULED : request.getStatus());
        return toResponse(repository.save(entry));
    }

    @Override
    @Transactional
    public WordOfTheDayResponse update(Long wordOfTheDayId, WordOfTheDayRequest request) {
        WordOfTheDay entry = repository.findById(wordOfTheDayId)
                .orElseThrow(() -> new ApiException("Word-of-the-day record not found with id: " + wordOfTheDayId));
        if (request == null) {
            throw new ApiException("Request body is required");
        }
        if (request.getWordId() != null) {
            entry.setWord(findWord(request.getWordId()));
        }
        if (request.getPublishOn() != null) {
            ensureDateIsAvailable(request.getPublishOn(), wordOfTheDayId);
            entry.setPublishOn(request.getPublishOn());
        }
        if (request.getStatus() != null) {
            entry.setStatus(request.getStatus());
        }
        return toResponse(repository.save(entry));
    }

    @Override
    @Transactional
    public void delete(Long wordOfTheDayId) {
        WordOfTheDay entry = repository.findById(wordOfTheDayId)
                .orElseThrow(() -> new ApiException("Word-of-the-day record not found with id: " + wordOfTheDayId));
        repository.delete(entry);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WordOfTheDayResponse> findBetween(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new ApiException("from and to must be valid dates and from must not be after to");
        }
        return repository.findByPublishOnBetweenOrderByPublishOnAsc(from, to)
                .stream().map(this::toResponse).toList();
    }

    @Scheduled(fixedDelayString = "${wotd.publish-check-delay-ms:60000}")
    @Transactional
    public void publishDueWords() {
        publishDueWords(LocalDate.now(ZoneId.of("Asia/Kolkata")));
    }

    private void publishDueWords(LocalDate today) {
        List<WordOfTheDay> dueEntries = repository.findByPublishOnLessThanEqualAndStatus(
                today, WordOfTheDayStatus.SCHEDULED);
        if (dueEntries.isEmpty()) {
            return;
        }
        dueEntries.forEach(entry -> entry.setStatus(WordOfTheDayStatus.PUBLISHED));
        repository.saveAll(dueEntries);
    }

    private void requireCreateFields(WordOfTheDayRequest request) {
        if (request == null || request.getWordId() == null || request.getPublishOn() == null) {
            throw new ApiException("wordId and publishOn are required");
        }
    }

    private void ensureDateIsAvailable(LocalDate publishOn, Long currentId) {
        repository.findByPublishOn(publishOn).ifPresent(existing -> {
            if (!existing.getId().equals(currentId)) {
                throw new ApiException("A word of the day already exists for " + publishOn);
            }
        });
    }

    private Word findWord(Long wordId) {
        return wordRepository.findById(wordId)
                .orElseThrow(() -> new ApiException("Word not found with id: " + wordId));
    }

    private WordOfTheDayResponse toResponse(WordOfTheDay entry) {
        return WordOfTheDayResponse.builder()
                // .id(entry.getId())
                .wordOfTheDayId(entry.getId())
            .wordId(entry.getWord().getId())
                .publishOn(entry.getPublishOn())
                .status(entry.getStatus())
                .createdAt(entry.getCreatedAt())
                .updatedAt(entry.getUpdatedAt())
                .build();
    }
}