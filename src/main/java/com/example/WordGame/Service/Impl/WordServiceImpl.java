package com.example.WordGame.Service.Impl;

import com.example.WordGame.DTO.Word.*;
import com.example.WordGame.Entities.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.WordGame.Repository.CategoryRepo;
import com.example.WordGame.Repository.WordExampleRepo;
import com.example.WordGame.Repository.WordRepo;
import com.example.WordGame.Service.WordService;
import com.example.WordGame.exceptions.ApiException;
import com.example.WordGame.exceptions.ResourceNotFoundExecption;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class WordServiceImpl implements WordService {

    private final WordRepo wordRepo;
    private final CategoryRepo categoryRepo;
    private final WordExampleRepo wordExampleRepo;
    private final ModelMapper modelMapper;
    private final AzureImageUploadService imageUploadService;
    private final ObjectMapper objectMapper;

    // ✅ ONLY ONE NEW METHOD - Random words with caching
    @Override
    public Page<WordResponseDTO> getRandomWords(Pageable pageable) {
        log.info("🎲 CACHE MISS - Fetching random words from DATABASE - Page: {}, Size: {}",
                pageable.getPageNumber(), pageable.getPageSize());

        // Get all words without category filter
        Page<Word> wordsPage = wordRepo.findAllWords(pageable);

        // Convert to DTO
        List<WordResponseDTO> dtos = wordsPage.getContent().stream()
                .map(this::convertToResponseDTO)
                .collect(Collectors.toList());

        // Shuffle for randomness
        Collections.shuffle(dtos);

        return new PageImpl<>(dtos, pageable, wordsPage.getTotalElements());
    }

    // ✅ Existing methods below...

    @Override
   // @Cacheable(value = "words", key = "#categoryName + '_' + #pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result == null")
    public Page<WordResponseDTO> getWordsByCategory(String categoryName, Pageable pageable) {
        log.info("📚 CACHE MISS - Fetching words for category '{}' page {} from DATABASE", categoryName, pageable.getPageNumber());

        Category category = categoryRepo.findByName(categoryName)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));

        return wordRepo.findByCategory(category, pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    @Cacheable(value = "wordDetails", key = "#wordId", unless = "#result == null")
    public WordDetailResponseDTO getWordDetail(Long wordId) {
        log.info("📚 CACHE MISS - Fetching word details for id {} from DATABASE", wordId);

        Word word = wordRepo.findById(wordId)
                .orElseThrow(() -> new ResourceNotFoundExecption("word", "wordId", wordId));

        List<String> examples = wordExampleRepo.findByWord(word).stream()
                .map(WordExample::getExample)
                .collect(Collectors.toList());

        if (examples.isEmpty()) {
            examples = parseExamples(word.getExamplesJson());
        }

        WordDetailResponseDTO responseDTO = modelMapper.map(word, WordDetailResponseDTO.class);
        responseDTO.setCategoryName(word.getCategory().getName());
        responseDTO.setExamples(examples);
        responseDTO.setMemeImageUrl(word.getMemeImageUrl());
        responseDTO.setImageUrl(word.getImageUrl());
        responseDTO.setFactsJson(word.getFactsJson());
        responseDTO.setExamplesJson(word.getExamplesJson());

        return responseDTO;
    }

    @Override
    @Cacheable(value = "wordDetails", key = "#id", unless = "#result == null")
    public WordResponseDTO getWordById(Long id) {
        log.info("📚 CACHE MISS - Fetching word by id {} from DATABASE", id);

        Word word = wordRepo.findById(id)
                .orElseThrow(() -> new ApiException("Word not found with id: " + id));
        return convertToResponseDTO(word);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "words", allEntries = true),
            @CacheEvict(value = "wordDetails", allEntries = true),
            @CacheEvict(value = "randomWords", allEntries = true)  // Clear random words cache
    })
    public WordResponseDTO createWord(WordRequestDTO request) {
        log.info("📝 Creating new word: {} - Will clear cache", request.getWord());

        Category category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiException("Category not found with id: " + request.getCategoryId()));

        Word word = new Word();
        word.setWord(request.getWord());
        word.setMeaning(request.getMeaning());
        word.setCategory(category);
        word.setCreatedAt(LocalDateTime.now());
        word.setImageUrl(resolveImageUrl(request));
        word.setFactsJson(resolveFactsJson(request));
        word.setExamplesJson(resolveExamplesJson(request));

        Word savedWord = wordRepo.save(word);

        persistExamples(savedWord, resolveExamples(request));

        log.info("✅ Word created successfully with ID: {}", savedWord.getId());
        return convertToResponseDTO(savedWord);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "words", allEntries = true),
            @CacheEvict(value = "wordDetails", key = "#id"),
            @CacheEvict(value = "randomWords", allEntries = true)  // Clear random words cache
    })
    public WordResponseDTO updateWord(Long id, WordRequestDTO request) {
        log.info("📝 Updating word {} - Will clear cache", id);

        Word word = wordRepo.findById(id)
                .orElseThrow(() -> new ApiException("Word not found with id: " + id));

        if (request.getWord() != null) {
            word.setWord(request.getWord());
        }

        if (request.getMeaning() != null) {
            word.setMeaning(request.getMeaning());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepo.findById(request.getCategoryId())
                    .orElseThrow(() -> new ApiException("Category not found with id: " + request.getCategoryId()));
            word.setCategory(category);
        }

        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            word.setImageUrl(request.getImageUrl());
        } else if (request.getMemeImage() != null && !request.getMemeImage().isEmpty()) {
            if (word.getImageUrl() != null) {
                imageUploadService.deleteImage(word.getImageUrl());
            }
            try {
                String imageUrl = imageUploadService.uploadImage(request.getMemeImage(), "words");
                word.setImageUrl(imageUrl);
            } catch (Exception e) {
                throw new ApiException("Failed to upload image: " + e.getMessage());
            }
        }

        if (request.getFactsJson() != null) {
            word.setFactsJson(request.getFactsJson());
        }

        if (request.getExamplesJson() != null) {
            word.setExamplesJson(request.getExamplesJson());
        } else if (request.getExamples() != null) {
            word.setExamplesJson(serializeExamples(request.getExamples()));
        }

        if (request.getExamplesJson() != null || request.getExamples() != null) {
            List<WordExample> existingExamples = wordExampleRepo.findByWord(word);
            wordExampleRepo.deleteAll(existingExamples);
            persistExamples(word, resolveExamples(request));
        }

        Word updatedWord = wordRepo.save(word);
        log.info("✅ Word updated successfully");
        return convertToResponseDTO(updatedWord);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "words", allEntries = true),
            @CacheEvict(value = "wordDetails", key = "#id"),
            @CacheEvict(value = "randomWords", allEntries = true)  // Clear random words cache
    })
    public void deleteWord(Long id) {
        log.info("🗑️ Deleting word {} - Will clear cache", id);

        Word word = wordRepo.findById(id)
                .orElseThrow(() -> new ApiException("Word not found with id: " + id));

        List<WordExample> examples = wordExampleRepo.findByWord(word);
        wordExampleRepo.deleteAll(examples);

        if (word.getImageUrl() != null) {
            imageUploadService.deleteImage(word.getImageUrl());
        }

        wordRepo.delete(word);
        log.info("✅ Word deleted successfully");
    }

    @Override
    @Transactional
    public List<WordResponseDTO> bulkCreateWords(Long categoryId, BulkWordImportDTO bulkRequest) {
        log.info("📦 Bulk creating {} words - Will clear cache", bulkRequest.getWords().size());

        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ApiException("Category not found with id: " + categoryId));

        List<WordResponseDTO> createdWords = new ArrayList<>();

        for (BulkWordImportDTO.WordEntry wordEntry : bulkRequest.getWords()) {
            Word word = new Word();
            word.setWord(wordEntry.getWord());
            word.setMeaning(wordEntry.getMeaning());
            word.setCategory(category);
            word.setCreatedAt(LocalDateTime.now());

            word.setImageUrl(resolveImageUrl(wordEntry));
            word.setFactsJson(wordEntry.getFactsJson());
            word.setExamplesJson(wordEntry.getExamplesJson());

            Word savedWord = wordRepo.save(word);

            persistExamples(savedWord, resolveExamples(wordEntry));

            createdWords.add(convertToResponseDTO(savedWord));
        }

        // Clear all caches
        evictAllWordCaches();

        return createdWords;
    }

    @Override
    public WordResponseDTO toggleWordStatus(Long id) {
        Word word = wordRepo.findById(id)
                .orElseThrow(() -> new ApiException("Word not found with id: " + id));
        return convertToResponseDTO(word);
    }

    private WordResponseDTO convertToResponseDTO(Word word) {
        WordResponseDTO responseDTO = modelMapper.map(word, WordResponseDTO.class);
        responseDTO.setCategoryName(word.getCategory().getName());
        responseDTO.setMemeImageUrl(word.getMemeImageUrl());
        responseDTO.setImageUrl(word.getImageUrl());
        responseDTO.setFactsJson(word.getFactsJson());
        responseDTO.setExamplesJson(word.getExamplesJson());
        return responseDTO;
    }

    private String resolveImageUrl(WordRequestDTO request) {
        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            return request.getImageUrl();
        }

        if (request.getMemeImage() != null && !request.getMemeImage().isEmpty()) {
            try {
                return imageUploadService.uploadImage(request.getMemeImage(), "words");
            } catch (Exception e) {
                throw new ApiException("Failed to upload image: " + e.getMessage());
            }
        }

        return null;
    }

    private String resolveImageUrl(BulkWordImportDTO.WordEntry wordEntry) {
        if (wordEntry.getImageUrl() != null && !wordEntry.getImageUrl().isBlank()) {
            return wordEntry.getImageUrl();
        }

        if (wordEntry.getMemeImageUrl() != null && !wordEntry.getMemeImageUrl().isBlank()) {
            return wordEntry.getMemeImageUrl();
        }

        return null;
    }

    private String resolveFactsJson(WordRequestDTO request) {
        return request.getFactsJson();
    }

    private String resolveExamplesJson(WordRequestDTO request) {
        if (request.getExamplesJson() != null) {
            return request.getExamplesJson();
        }

        if (request.getExamples() != null) {
            return serializeExamples(request.getExamples());
        }

        return null;
    }

    private List<String> resolveExamples(WordRequestDTO request) {
        if (request.getExamples() != null) {
            return request.getExamples();
        }

        return parseExamples(request.getExamplesJson());
    }

    private List<String> resolveExamples(BulkWordImportDTO.WordEntry wordEntry) {
        if (wordEntry.getExamples() != null) {
            return wordEntry.getExamples();
        }

        return parseExamples(wordEntry.getExamplesJson());
    }

    private void persistExamples(Word word, List<String> examples) {
        if (examples == null || examples.isEmpty()) {
            return;
        }

        for (String exampleText : examples) {
            if (exampleText != null && !exampleText.trim().isEmpty()) {
                WordExample example = new WordExample();
                example.setWord(word);
                example.setExample(exampleText);
                wordExampleRepo.save(example);
            }
        }
    }

    private String serializeExamples(List<String> examples) {
        try {
            return objectMapper.writeValueAsString(examples);
        } catch (Exception e) {
            throw new ApiException("Failed to serialize examples: " + e.getMessage());
        }
    }

    private List<String> parseExamples(String examplesJson) {
        if (examplesJson == null || examplesJson.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(examplesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse examples JSON: {}", e.getMessage());
            return Collections.singletonList(examplesJson);
        }
    }

    private void evictAllWordCaches() {
        log.info("🗑️ Evicting all word caches");
        // Programmatic cache eviction if needed
    }
}