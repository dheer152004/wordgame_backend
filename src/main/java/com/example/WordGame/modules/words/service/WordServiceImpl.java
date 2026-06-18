package com.example.WordGame.modules.words.service;

// import com.example.WordGame.Entities.*;
import com.example.WordGame.Service.AzureImageUploadService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.WordGame.exceptions.ApiException;
import com.example.WordGame.exceptions.ResourceNotFoundExecption;
import com.example.WordGame.modules.category.Entities.Category;
import com.example.WordGame.modules.category.repository.CategoryRepo;
import com.example.WordGame.modules.words.DTO.*;
import com.example.WordGame.modules.words.Entities.Word;
import com.example.WordGame.modules.words.repository.WordRepo;

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
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class WordServiceImpl implements WordService {

    private final WordRepo wordRepo;
    private final CategoryRepo categoryRepo;
    // private final WordExampleRepo wordExampleRepo;
    // private final com.example.WordGame.Repository.WordRelationRepo wordRelationRepo;
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
    @Cacheable(value = "wordDetails", key = "#id", unless = "#result == null")
    public WordResponseDTO getWordById(Long id) {
        log.info("📚 CACHE MISS - Fetching word by id {} from DATABASE", id);

        Word word = wordRepo.findById(id)
                .orElseThrow(() -> new ApiException("Word not found with id: " + id));
        return convertToResponseDTO(word);
    }

    @Override
    @Cacheable(value = "wordDetails", key = "#wordId", unless = "#result == null")
    public WordDetailResponseDTO getWordDetail(Long wordId) {
        log.info("📚 CACHE MISS - Fetching word details for id {} from DATABASE", wordId);

        Word word = wordRepo.findById(wordId)
                .orElseThrow(() -> new ResourceNotFoundExecption("word", "wordId", wordId));

        WordDetailResponseDTO responseDTO = new WordDetailResponseDTO();
        responseDTO.setId(word.getId());
        responseDTO.setWord(word.getWord());
        responseDTO.setCategoryId(word.getCategory() != null ? word.getCategory().getId() : null);
        responseDTO.setMeaning(word.getMeaning());
        responseDTO.setDescription(word.getDescription());
        responseDTO.setImages(word.getImages());
        responseDTO.setSourceAndCredits(parseSourceCredits(word.getSourceCreditsJson()));
        responseDTO.setFacts(parseFacts(word.getFactsJson()));
        responseDTO.setExamples(parseExamples(word.getExamplesJson()));
        responseDTO.setCreated(formatDateTime(word.getCreatedAt()));
        responseDTO.setUpdated(formatDateTime(word.getUpdatedAt()));

        // related words: not implemented - return empty list for now
        responseDTO.setRelatedWordIds(Collections.emptyList());

        // alsoAppearsIn
        responseDTO.setAlsoAppearsIn(parseAlsoAppearsIn(word.getAlsoAppearsInJson()));

        return responseDTO;
    }

    @Override
    public WordResponseDTO getWordResponseDTO(Long id) {
        // simple wrapper to return the WordResponseDTO for a word id
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

        // validate required fields
        if (request.getWord() == null || request.getWord().isBlank()) {
            throw new ApiException("Word is required");
        }
        if (request.getMeaning() == null || request.getMeaning().isBlank()) {
            throw new ApiException("Meaning is required");
        }

        if (request.getCategoryId() == null) {
            throw new ApiException("CategoryId is required");
        }

        Category category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiException("Category not found with id: " + request.getCategoryId()));

        Word word = new Word();
        word.setWord(request.getWord());
        word.setMeaning(request.getMeaning());
        if (request.getDescription() != null) {
            word.setDescription(request.getDescription());
        }

        if (request.getSourceAndCredits() != null) {
            word.setSourceCreditsJson(serializeSourceCredits(request.getSourceAndCredits()));
        }
        word.setCategory(category);
        word.setCreatedAt(LocalDateTime.now());
        word.setUpdatedAt(LocalDateTime.now());
        List<String> images = resolveImagesJson(request);
        if (images != null) word.setImages(images);
        word.setFactsJson(resolveFactsJson(request));
        word.setExamplesJson(resolveExamplesJson(request));
        if (request.getSourceAndCredits() != null) {
            word.setSourceCreditsJson(serializeSourceCredits(request.getSourceAndCredits()));
        }

        Word savedWord = wordRepo.save(word);

        // persistExamples(savedWord, resolveExamples(request));

        // handle related words
        // if (request.getRelatedWordIds() != null && !request.getRelatedWordIds().isEmpty()) {
        //     // remove any existing relations for this word
        //     List<com.example.WordGame.Entities.WordRelation> existing = wordRelationRepo.findByWordId(savedWord.getId());
        //     if (existing != null && !existing.isEmpty()) {
        //         wordRelationRepo.deleteAll(existing);
        //     }
        //     for (Long relatedId : request.getRelatedWordIds()) {
        //         Word related = wordRepo.findById(relatedId)
        //                 .orElseThrow(() -> new ApiException("Related word not found with id: " + relatedId));
        //         com.example.WordGame.Entities.WordRelation relation = new com.example.WordGame.Entities.WordRelation();
        //         relation.setWord(savedWord);
        //         relation.setRelatedWord(related);
        //         relation.setRelationType("related");
        //         relation.setCreatedAt(LocalDateTime.now());
        //         wordRelationRepo.save(relation);
        //     }
        // }

        // handle alsoAppearsIn
        if (request.getAlsoAppearsIn() != null) {
            savedWord.setAlsoAppearsInJson(serializeAlsoAppearsIn(request.getAlsoAppearsIn()));
            wordRepo.save(savedWord);
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

        if (request.getWord() != null) {
            if (request.getWord().isBlank()) {
                throw new ApiException("Word cannot be empty");
            }
            word.setWord(request.getWord());
        }

        if (request.getMeaning() != null) {
            if (request.getMeaning().isBlank()) {
                throw new ApiException("Meaning cannot be empty");
            }
            word.setMeaning(request.getMeaning());
        }

        if (request.getDescription() != null) {
            word.setDescription(request.getDescription());
        }

        if (request.getCategoryId() != null) {
            Category category = categoryRepo.findById(request.getCategoryId())
                    .orElseThrow(() -> new ApiException("Category not found with id: " + request.getCategoryId()));
            word.setCategory(category);
        }

        // handle multiple images
        if ((request.getImageUrls() != null && !request.getImageUrls().isEmpty()) || (request.getWordImages() != null && !request.getWordImages().isEmpty())) {
            // delete old images if any
            List<String> oldImages = word.getImages();
            for (String old : oldImages) {
                if (old != null && !old.isBlank()) {
                    try { imageUploadService.deleteImage(old); } catch (Exception ignored) {}
                }
            }
            List<String> images = resolveImagesJson(request);
            word.setImages(images);
        }

        if (request.getFacts() != null) {
            word.setFactsJson(serializeFacts(request.getFacts()));
        }

        if (request.getExamples() != null) {
            word.setExamplesJson(serializeExamples(request.getExamples()));
        }

        // if (request.getExamples() != null) {
        //     List<WordExample> existingExamples = wordExampleRepo.findByWord(word);
        //     wordExampleRepo.deleteAll(existingExamples);
        //     persistExamples(word, resolveExamples(request));
        // }

        // // handle related words on update: replace existing relations if provided
        // if (request.getRelatedWordIds() != null) {
        //     List<com.example.WordGame.Entities.WordRelation> existing = wordRelationRepo.findByWordId(word.getId());
        //     if (existing != null && !existing.isEmpty()) {
        //         wordRelationRepo.deleteAll(existing);
        //     }
        //     for (Long relatedId : request.getRelatedWordIds()) {
        //         Word related = wordRepo.findById(relatedId)
        //                 .orElseThrow(() -> new ApiException("Related word not found with id: " + relatedId));
        //         com.example.WordGame.Entities.WordRelation relation = new com.example.WordGame.Entities.WordRelation();
        //         relation.setWord(word);
        //         relation.setRelatedWord(related);
        //         relation.setRelationType("related");
        //         relation.setCreatedAt(LocalDateTime.now());
        //         wordRelationRepo.save(relation);
        //     }
        // }

        // handle alsoAppearsIn on update
        if (request.getAlsoAppearsIn() != null) {
            word.setAlsoAppearsInJson(serializeAlsoAppearsIn(request.getAlsoAppearsIn()));
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

        // List<WordExample> examples = wordExampleRepo.findByWord(word);
        // wordExampleRepo.deleteAll(examples);

        List<String> imgs = word.getImages();
        for (String i : imgs) {
            if (i != null && !i.isBlank()) {
                try { imageUploadService.deleteImage(i); } catch (Exception ignored) {}
            }
        }
        word.setUpdatedAt(LocalDateTime.now());

        wordRepo.delete(word);
        log.info("✅ Word deleted successfully");
    }

    // @Override
    // @Transactional
    // public List<WordResponseDTO> bulkCreateWords(Long categoryId, BulkWordImportDTO bulkRequest) {
        // log.info("📦 Bulk creating {} words - Will clear cache", bulkRequest.getWords().size());

        // Category category = categoryRepo.findById(categoryId)
        //         .orElseThrow(() -> new ApiException("Category not found with id: " + categoryId));

        // List<WordResponseDTO> createdWords = new ArrayList<>();

        // for (BulkWordImportDTO.WordEntry wordEntry : bulkRequest.getWords()) {
        //     Word word = new Word();
        //     word.setWord(wordEntry.getWord());
        //     word.setMeaning(wordEntry.getMeaning());
        //     word.setCategory(category);
        //     word.setCreatedAt(LocalDateTime.now());

        //     List<String> imgs = resolveImagesJson(wordEntry);
        //     if (imgs != null) word.setImages(imgs);
        //     word.setFactsJson(wordEntry.getFactsJson());
        //         word.setUpdatedAt(LocalDateTime.now());
        //     word.setExamplesJson(wordEntry.getExamplesJson());

        //     Word savedWord = wordRepo.save(word);

        //     persistExamples(savedWord, resolveExamples(wordEntry));

        //     createdWords.add(convertToResponseDTO(savedWord));
        // }

    //     // Clear all caches
    //     evictAllWordCaches();

    //     return createdWords;
    // }

    @Override
    public WordResponseDTO toggleWordStatus(Long id) {
        Word word = wordRepo.findById(id)
                .orElseThrow(() -> new ApiException("Word not found with id: " + id));
        return convertToResponseDTO(word);
    }

    private WordResponseDTO convertToResponseDTO(Word word) {
        WordResponseDTO responseDTO = new WordResponseDTO();
        responseDTO.setId(word.getId());
        responseDTO.setWord(word.getWord());
        responseDTO.setCategoryId(word.getCategory() != null ? word.getCategory().getId() : null);
        responseDTO.setMeaning(word.getMeaning());
        List<String> imgs = word.getImages();
        responseDTO.setImages(imgs);
        responseDTO.setFacts(parseFacts(word.getFactsJson()));
        responseDTO.setExamples(parseExamples(word.getExamplesJson()));
        responseDTO.setCreated(formatDateTime(word.getCreatedAt()));
        responseDTO.setUpdated(formatDateTime(word.getUpdatedAt()));
        responseDTO.setDescription(word.getDescription());
        responseDTO.setSourceAndCredits(parseSourceCredits(word.getSourceCreditsJson()));

        // related words
        // List<com.example.WordGame.Entities.WordRelation> relations = wordRelationRepo.findByWordId(word.getId());
        // List<WordResponseDTO.RelatedWord> related = relations.stream()
        //         .map(r -> new WordResponseDTO.RelatedWord(r.getRelatedWord().getId(), r.getRelatedWord().getWord()))
        //         .collect(Collectors.toList());
        // responseDTO.setRelatedWordIds(related);

        // alsoAppearsIn
        responseDTO.setAlsoAppearsIn(parseAlsoAppearsIn(word.getAlsoAppearsInJson()));

        return responseDTO;
    }

    private List<String> resolveImagesJson(WordRequestDTO request) {
        List<String> urls = new ArrayList<>();
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            urls.addAll(request.getImageUrls());
        }

        if (request.getWordImages() != null && !request.getWordImages().isEmpty()) {
            for (org.springframework.web.multipart.MultipartFile mf : request.getWordImages()) {
                if (mf != null && !mf.isEmpty()) {
                    try {
                        String u = imageUploadService.uploadImage(mf, "words");
                        urls.add(u);
                    } catch (Exception e) {
                        throw new ApiException("Failed to upload image: " + e.getMessage());
                    }
                }
            }
        }

        if (urls.isEmpty()) return null;
        return urls;
    }

    // private List<String> resolveImagesJson(BulkWordImportDTO.WordEntry wordEntry) {
    //     if (wordEntry.getImages() != null && !wordEntry.getImages().isEmpty()) {
    //         return wordEntry.getImages();
    //     }
    //     return null;
    // }

    private String resolveFactsJson(WordRequestDTO request) {
        if (request.getFacts() != null) {
            return serializeFacts(request.getFacts());
        }
        return null;
    }

    private String resolveExamplesJson(WordRequestDTO request) {
        if (request.getExamples() != null) {
            return serializeExamples(request.getExamples());
        }
        return null;
    }

    private List<String> resolveExamples(WordRequestDTO request) {
        if (request.getExamples() != null) {
            return request.getExamples();
        }
        return Collections.emptyList();
    }

    // private List<String> resolveExamples(BulkWordImportDTO.WordEntry wordEntry) {
    //     if (wordEntry.getExamples() != null) {
    //         return wordEntry.getExamples();
    //     }

    //     return parseExamples(wordEntry.getExamplesJson());
    // }

    // private void persistExamples(Word word, List<String> examples) {
    //     if (examples == null || examples.isEmpty()) {
    //         return;
    //     }

    //     for (String exampleText : examples) {
    //         if (exampleText != null && !exampleText.trim().isEmpty()) {
    //             WordExample example = new WordExample();
    //             example.setWord(word);
    //             example.setExample(exampleText);
    //             wordExampleRepo.save(example);
    //         }
    //     }
    // }

    private String serializeExamples(List<String> examples) {
        try {
            return objectMapper.writeValueAsString(examples);
        } catch (Exception e) {
            throw new ApiException("Failed to serialize examples: " + e.getMessage());
        }
    }

    private String serializeFacts(List<String> facts) {
        try {
            return objectMapper.writeValueAsString(facts);
        } catch (Exception e) {
            throw new ApiException("Failed to serialize facts: " + e.getMessage());
        }
    }

    private String serializeAlsoAppearsIn(List<Map<String, Long>> alsoAppearsIn) {
        try {
            return objectMapper.writeValueAsString(alsoAppearsIn);
        } catch (Exception e) {
            throw new ApiException("Failed to serialize alsoAppearsIn: " + e.getMessage());
        }
    }

    private String formatDateTime(LocalDateTime dt) {
        if (dt == null) return null;
        return dt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
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

    private String serializeImages(List<String> images) {
        try {
            return objectMapper.writeValueAsString(images);
        } catch (Exception e) {
            throw new ApiException("Failed to serialize images: " + e.getMessage());
        }
    }

    private List<String> parseImages(String imagesJson) {
        if (imagesJson == null || imagesJson.isBlank()) return Collections.emptyList();
        try {
            return objectMapper.readValue(imagesJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse images JSON: {}", e.getMessage());
            return Collections.singletonList(imagesJson);
        }
    }

    private List<String> parseFacts(String factsJson) {
        if (factsJson == null || factsJson.isBlank()) {
            return Collections.emptyList();
        }

        try {
            return objectMapper.readValue(factsJson, new TypeReference<List<String>>() {});
        } catch (Exception e) {
            log.warn("Failed to parse facts JSON: {}", e.getMessage());
            return Collections.singletonList(factsJson);
        }
    }

    private List<WordResponseDTO.AlsoAppearsIn> parseAlsoAppearsIn(String json) {
        if (json == null || json.isBlank()) return Collections.emptyList();
        try {
            List<Map<String, Long>> raw = objectMapper.readValue(json, new TypeReference<List<Map<String, Long>>>() {});
            return raw.stream()
                    .map(m -> new WordResponseDTO.AlsoAppearsIn(m.getOrDefault("categoryId", null), m.getOrDefault("wordId", null)))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("Failed to parse alsoAppearsIn JSON: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private String serializeSourceCredits(SourceCreditsDTO src) {
        if (src == null) return null;
        try {
            return objectMapper.writeValueAsString(src);
        } catch (Exception e) {
            throw new ApiException("Failed to serialize source credits: " + e.getMessage());
        }
    }

    private SourceCreditsDTO parseSourceCredits(String json) {
        if (json == null || json.isBlank()) return null;
        try {
            return objectMapper.readValue(json, SourceCreditsDTO.class);
        } catch (Exception e) {
            log.warn("Failed to parse sourceCredits JSON: {}", e.getMessage());
            return null;
        }
    }

    private void evictAllWordCaches() {
        log.info("🗑️ Evicting all word caches");
        // Programmatic cache eviction if needed
    }
}