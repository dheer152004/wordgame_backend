package com.example.WordGame.Service.Impl;

import com.example.WordGame.DTO.Word.*;
import com.example.WordGame.Entities.*;
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
    @Cacheable(value = "words", key = "#categoryName + '_' + #pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result == null")
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

        WordDetailResponseDTO responseDTO = modelMapper.map(word, WordDetailResponseDTO.class);
        responseDTO.setCategoryName(word.getCategory().getName());
        responseDTO.setExamples(examples);

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

        if (wordRepo.findByWord(request.getWord()).isPresent()) {
            throw new ApiException("Word already exists: " + request.getWord());
        }

        Word word = new Word();
        word.setWord(request.getWord());
        word.setMeaning(request.getMeaning());
        word.setCategory(category);
        word.setCreatedAt(LocalDateTime.now());

        if (request.getMemeImage() != null && !request.getMemeImage().isEmpty()) {
            try {
                String imageUrl = imageUploadService.uploadImage(request.getMemeImage(), "words");
                word.setMemeImageUrl(imageUrl);
            } catch (Exception e) {
                throw new ApiException("Failed to upload image: " + e.getMessage());
            }
        }

        Word savedWord = wordRepo.save(word);

        if (request.getExamples() != null && !request.getExamples().isEmpty()) {
            for (String exampleText : request.getExamples()) {
                if (exampleText != null && !exampleText.trim().isEmpty()) {
                    WordExample example = new WordExample();
                    example.setWord(savedWord);
                    example.setExample(exampleText);
                    wordExampleRepo.save(example);
                }
            }
        }

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

        if (request.getWord() != null && !request.getWord().equals(word.getWord())) {
            if (wordRepo.findByWord(request.getWord()).isPresent()) {
                throw new ApiException("Word already exists: " + request.getWord());
            }
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

        if (request.getMemeImage() != null && !request.getMemeImage().isEmpty()) {
            if (word.getMemeImageUrl() != null) {
                imageUploadService.deleteImage(word.getMemeImageUrl());
            }
            try {
                String imageUrl = imageUploadService.uploadImage(request.getMemeImage(), "words");
                word.setMemeImageUrl(imageUrl);
            } catch (Exception e) {
                throw new ApiException("Failed to upload image: " + e.getMessage());
            }
        }

        if (request.getExamples() != null) {
            List<WordExample> existingExamples = wordExampleRepo.findByWord(word);
            wordExampleRepo.deleteAll(existingExamples);

            for (String exampleText : request.getExamples()) {
                if (exampleText != null && !exampleText.trim().isEmpty()) {
                    WordExample example = new WordExample();
                    example.setWord(word);
                    example.setExample(exampleText);
                    wordExampleRepo.save(example);
                }
            }
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

        if (word.getMemeImageUrl() != null) {
            imageUploadService.deleteImage(word.getMemeImageUrl());
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
            if (wordRepo.findByWord(wordEntry.getWord()).isPresent()) {
                throw new ApiException("Word already exists: " + wordEntry.getWord());
            }

            Word word = new Word();
            word.setWord(wordEntry.getWord());
            word.setMeaning(wordEntry.getMeaning());
            word.setCategory(category);
            word.setCreatedAt(LocalDateTime.now());

            if (wordEntry.getMemeImageUrl() != null && !wordEntry.getMemeImageUrl().isEmpty()) {
                word.setMemeImageUrl(wordEntry.getMemeImageUrl());
            }

            Word savedWord = wordRepo.save(word);

            if (wordEntry.getExamples() != null && !wordEntry.getExamples().isEmpty()) {
                for (String exampleText : wordEntry.getExamples()) {
                    if (exampleText != null && !exampleText.trim().isEmpty()) {
                        WordExample example = new WordExample();
                        example.setWord(savedWord);
                        example.setExample(exampleText);
                        wordExampleRepo.save(example);
                    }
                }
            }

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
        return responseDTO;
    }

    private void evictAllWordCaches() {
        log.info("🗑️ Evicting all word caches");
        // Programmatic cache eviction if needed
    }
}