package com.example.WordGame.modules.words.service;

// import com.example.WordGame.Entities.*;
import com.example.WordGame.Service.ImageStorageService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.WordGame.exceptions.ApiException;
import com.example.WordGame.exceptions.ResourceNotFoundExecption;
import com.example.WordGame.modules.category.Entities.Category;
import com.example.WordGame.modules.category.repository.CategoryRepo;
import com.example.WordGame.modules.words.DTO.*;
import com.example.WordGame.modules.words.Entities.Word;
import com.example.WordGame.modules.words.WordType;
import com.example.WordGame.modules.words.repository.WordRepo;
import com.example.WordGame.modules.roles.user.Entities.UserProfile;
import com.example.WordGame.modules.roles.user.profile.UserProfileRepository;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDate;
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
    private final ImageStorageService imageUploadService;
    private final ObjectMapper objectMapper;
    @Autowired
    private UserProfileRepository userProfileRepository;

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

    @Override
    public Page<WordResponseDTO> searchWords(String q, Pageable pageable) {
        log.info("🔎 Searching words for query '{}' - page {} size {}", q, pageable.getPageNumber(), pageable.getPageSize());
        if (q == null || q.isBlank()) {
            return Page.empty(pageable);
        }
        Page<Word> wordsPage = wordRepo.findByWordIgnoreCaseContaining(q.trim(), pageable);
        List<WordResponseDTO> dtos = wordsPage.getContent().stream().map(this::convertToResponseDTO).collect(Collectors.toList());
        return new PageImpl<>(dtos, pageable, wordsPage.getTotalElements());
    }

    // ✅ Existing methods below...

    @Override
   // @Cacheable(value = "words", key = "#categoryName + '_' + #pageable.pageNumber + '_' + #pageable.pageSize", unless = "#result == null")
    public Page<WordResponseDTO> getWordsByCategory(String categoryName, Pageable pageable) {
        Category category = resolveCategory(categoryName);
        return wordRepo.findByCategory(category, pageable).map(this::convertToResponseDTO);
    }

    private Page<WordResponseDTO> getWordsByCategoryForAge(String categoryName, Pageable pageable, Integer age) {
        log.info("📚 CACHE MISS - Fetching words for category key '{}' page {} from DATABASE", categoryName, pageable.getPageNumber());

        Category category = resolveCategory(categoryName);

        if (age != null && age < 0) {
            throw new ApiException("Age cannot be negative");
        }

        return wordRepo.findByCategoryAndAge(category, age,
                        com.example.WordGame.modules.category.enums.AgeRating.ALL,
                        com.example.WordGame.modules.category.enums.AgeRating._13_PLUS,
                        com.example.WordGame.modules.category.enums.AgeRating._16_PLUS,
                        com.example.WordGame.modules.category.enums.AgeRating._18_PLUS,
                        pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    public Page<WordResponseDTO> getWordsByCategory(String categoryName, Pageable pageable, String userEmail) {
        UserProfile profile = null;
        if (userEmail != null && !userEmail.isBlank()) {
            profile = userProfileRepository.findByUser_Username(userEmail).orElse(null);
        }

        Integer age = profile == null ? null : calculateAge(profile.getDateOfBirth());
        return getWordsByCategoryForAge(categoryName, pageable, age);
    }

    private Integer calculateAge(LocalDate dateOfBirth) {
        if (dateOfBirth == null) return null;
        return java.time.Period.between(dateOfBirth, LocalDate.now()).getYears();
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
        responseDTO.setWordType(word.getWordType());
        responseDTO.setExpandedForm(word.getExpandedForm());
        responseDTO.setPartOfSpeech(word.getPartOfSpeech());
        responseDTO.setCategoryId(word.getCategory() != null ? word.getCategory().getId() : null);
        responseDTO.setCategoryName(word.getCategory() != null ? word.getCategory().getName() : null);
        responseDTO.setMeaning(word.getMeaning());
        responseDTO.setDescription(word.getDescription());
        responseDTO.setImages(toImageUrls(word.getImages()));
        responseDTO.setVideos(toVideoUrls(word.getVideos()));
        responseDTO.setAudios(toAudioUrls(word.getAudios()));
        responseDTO.setSourceAndCredits(parseSourceCredits(word.getSourceCreditsJson()));
        responseDTO.setFacts(parseFacts(word.getFactsJson()));
        responseDTO.setExamples(parseExamples(word.getExamplesJson()));
        responseDTO.setCreated(formatDateTime(word.getCreatedAt()));
        responseDTO.setUpdated(formatDateTime(word.getUpdatedAt()));
        responseDTO.setDisplayOrder(word.getDisplayOrder());

        // related words - resolve persisted related IDs into response objects
        List<Long> relatedIds = word.getRelatedWordIds();
        if (relatedIds == null || relatedIds.isEmpty()) {
            responseDTO.setRelatedWordIds(Collections.emptyList());
        } else {
            List<WordResponseDTO.RelatedWord> related = new ArrayList<>();
            for (Long rid : relatedIds) {
                wordRepo.findById(rid).ifPresentOrElse(
                        rw -> related.add(new WordResponseDTO.RelatedWord(rw.getId(), rw.getWord())),
                        () -> related.add(new WordResponseDTO.RelatedWord(rid, null)));
            }
            responseDTO.setRelatedWordIds(related);
        }

        // alsoAppearsIn
        responseDTO.setAlsoAppearsIn(parseAlsoAppearsIn(word.getAlsoAppearsInJson()));

        // quiz modes
        responseDTO.setQuizModes(mapQuizModes(word.getQuizModes()));

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
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "words", allEntries = true),
            @CacheEvict(value = "wordDetails", allEntries = true),
            @CacheEvict(value = "randomWords", allEntries = true)  // Clear random words cache
    })
    public WordResponseDTO createWord(WordRequestDTO request) {
        log.info("📝 Creating new word: {} - Will clear cache", request.getWord());
        log.info("📝 createWord received quizModes: {}", request.getQuizModes());

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
        word.setWordType(request.getWordType() != null ? request.getWordType() : WordType.NORMAL_WORD);
        word.setExpandedForm(request.getExpandedForm());
        word.setPartOfSpeech(request.getPartOfSpeech());
        word.setMeaning(request.getMeaning());
        if (request.getDescription() != null) {
            word.setDescription(request.getDescription());
        }

        if (request.getSourceAndCredits() != null) {
            word.setSourceCreditsJson(serializeSourceCredits(request.getSourceAndCredits()));
        }
        word.setCategory(category);
        if (request.getDisplayOrder() != null) {
            word.setDisplayOrder(request.getDisplayOrder());
        } else {
            word.setDisplayOrder(deriveNextDisplayOrder(category));
        }
        word.setCreatedAt(LocalDateTime.now());
        word.setUpdatedAt(LocalDateTime.now());
        word.setFactsJson(resolveFactsJson(request));
        word.setExamplesJson(resolveExamplesJson(request));
        // quiz modes
        if (request.getQuizModes() != null && !request.getQuizModes().isEmpty()) {
            java.util.Set<com.example.WordGame.modules.words.QuizMode> modes = new java.util.LinkedHashSet<>();
            for (String m : request.getQuizModes()) {
                try {
                    modes.add(com.example.WordGame.modules.words.QuizMode.valueOf(m.trim().toUpperCase()));
                } catch (Exception ignored) {}
            }
            word.setQuizModes(modes);
            log.info("📝 Persisting quizModes for word (to save): {}", word.getQuizModes());
        }
        // persist related ids if provided
        if (request.getRelatedWordIds() != null && !request.getRelatedWordIds().isEmpty()) {
            word.setRelatedWordIds(request.getRelatedWordIds());
        }
        if (request.getSourceAndCredits() != null) {
            word.setSourceCreditsJson(serializeSourceCredits(request.getSourceAndCredits()));
        }


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

        // handle alsoAppearsIn - attach to the word before saving
        if (request.getAlsoAppearsIn() != null) {
            word.setAlsoAppearsInJson(serializeAlsoAppearsIn(request.getAlsoAppearsIn()));
        }

        Word savedWord = wordRepo.save(word);

        List<String> images = resolveImagesJson(request, savedWord.getWord(), savedWord.getId());
        if (images != null) savedWord.setImages(images);
        savedWord.setVideos(resolveVideoUrls(request, savedWord.getWord(), savedWord.getId()));
        savedWord.setAudios(resolveAudioUrls(request, savedWord.getWord(), savedWord.getId()));
        savedWord = wordRepo.save(savedWord);

        // build response DTO
        WordResponseDTO responseDTO = convertToResponseDTO(savedWord);
        log.info("📝 createWord returning response quizModes: {}", responseDTO.getQuizModes());

        log.info("✅ Word created successfully with ID: {}", savedWord.getId());
        return responseDTO;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "words", allEntries = true),
            @CacheEvict(value = "wordDetails", key = "#id"),
            @CacheEvict(value = "randomWords", allEntries = true)  // Clear random words cache
    })
    public WordResponseDTO updateWord(Long id, WordRequestDTO request) {
        log.info("📝 Updating word {} - Will clear cache", id);
        log.info("📝 updateWord received quizModes: {}", request.getQuizModes());
        
        Word word = wordRepo.findById(id)
                .orElseThrow(() -> new ApiException("Word not found with id: " + id));

        if (request.getWord() != null) {
            if (request.getWord().isBlank()) {
                throw new ApiException("Word cannot be empty");
            }
            word.setWord(request.getWord());
        }

        if (request.getWordType() != null) {
            word.setWordType(request.getWordType());
        }

        if (request.getExpandedForm() != null) {
            word.setExpandedForm(request.getExpandedForm());
        }

        if (request.getPartOfSpeech() != null) {
            word.setPartOfSpeech(request.getPartOfSpeech());
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
        if ((request.getImages() != null && !request.getImages().isEmpty())
            || (request.getImageUrls() != null && !request.getImageUrls().isEmpty())
            || (request.getWordImages() != null && !request.getWordImages().isEmpty())) {
            // delete old images if any
            List<String> oldImages = word.getImages();
            for (String old : oldImages) {
                if (old != null && !old.isBlank()) {
                    try { imageUploadService.deleteImage(old); } catch (Exception ignored) {}
                }
            }
            List<String> images = resolveImagesJson(request, word.getWord(), word.getId());
            word.setImages(images);
        }
        if ((request.getVideos() != null && !request.getVideos().isEmpty())
                || (request.getWordVideos() != null && !request.getWordVideos().isEmpty())) {
            deleteMediaFiles(word.getVideos());
            word.setVideos(resolveVideoUrls(request, word.getWord(), word.getId()));
        }
        if ((request.getAudios() != null && !request.getAudios().isEmpty())
                || (request.getWordAudios() != null && !request.getWordAudios().isEmpty())) {
            deleteMediaFiles(word.getAudios());
            word.setAudios(resolveAudioUrls(request, word.getWord(), word.getId()));
        }

        if (request.getFacts() != null) {
            word.setFactsJson(serializeFacts(request.getFacts()));
        }

        if (request.getExamples() != null) {
            word.setExamplesJson(serializeExamples(request.getExamples()));
        }

        if (request.getDisplayOrder() != null) {
            word.setDisplayOrder(request.getDisplayOrder());
            log.info("📝 updateWord set displayOrder on entity: {}", word.getDisplayOrder());
        }

        if (request.getQuizModes() != null) {
            java.util.Set<com.example.WordGame.modules.words.QuizMode> modes = new java.util.LinkedHashSet<>();
            for (String m : request.getQuizModes()) {
                try { modes.add(com.example.WordGame.modules.words.QuizMode.valueOf(m.trim().toUpperCase())); } catch (Exception ignored) {}
            }
            word.setQuizModes(modes);
            log.info("📝 updateWord set quizModes on entity: {}", word.getQuizModes());
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

        WordResponseDTO responseDTO = convertToResponseDTO(updatedWord);
        log.info("📝 updateWord returning response quizModes: {}", responseDTO.getQuizModes());
        if (request.getRelatedWordIds() != null) {
            // update persisted related ids as requested
            updatedWord.setRelatedWordIds(request.getRelatedWordIds());
            wordRepo.save(updatedWord);
            List<WordResponseDTO.RelatedWord> related = new ArrayList<>();
            for (Long relatedId : request.getRelatedWordIds()) {
                wordRepo.findById(relatedId).ifPresent(rw -> related.add(new WordResponseDTO.RelatedWord(rw.getId(), rw.getWord())));
                if (related.stream().noneMatch(r -> r.getWordId().equals(relatedId))) {
                    related.add(new WordResponseDTO.RelatedWord(relatedId, null));
                }
            }
            responseDTO.setRelatedWordIds(related);
        }

        return responseDTO;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "words", allEntries = true),
            @CacheEvict(value = "wordDetails", key = "#id"),
            @CacheEvict(value = "randomWords", allEntries = true)  // Clear random words cache
    })
    public WordResponseDTO updateWordDisplayOrder(Long id, Long displayOrder) {
        log.info("📝 Updating displayOrder for word {} to {}", id, displayOrder);
        if (displayOrder == null) {
            throw new ApiException("displayOrder is required");
        }
        Word word = wordRepo.findById(id)
                .orElseThrow(() -> new ApiException("Word not found with id: " + id));
        word.setDisplayOrder(displayOrder);
        word.setUpdatedAt(LocalDateTime.now());
        Word updatedWord = wordRepo.save(word);
        return convertToResponseDTO(updatedWord);
    }

    private static final long DISPLAY_ORDER_INITIAL = 10000L;
    private static final long DISPLAY_ORDER_INCREMENT = 10000L;

    public long deriveNextDisplayOrder(Category category) {
        Long maxOrder = wordRepo.findMaxDisplayOrderByCategory(category);
        if (maxOrder == null || maxOrder < DISPLAY_ORDER_INITIAL) {
            return DISPLAY_ORDER_INITIAL;
        }
        return maxOrder + DISPLAY_ORDER_INCREMENT;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "words", allEntries = true),
            @CacheEvict(value = "wordDetails", allEntries = true),
            @CacheEvict(value = "randomWords", allEntries = true)
    })
    public int rebalanceDisplayOrder() {
        List<com.example.WordGame.modules.category.Entities.Category> categories = categoryRepo.findAll();
        if (categories == null || categories.isEmpty()) {
            return 0;
        }

        int totalUpdated = 0;

        for (com.example.WordGame.modules.category.Entities.Category category : categories) {
            List<Word> words = wordRepo.findAllByCategoryId(category.getId());
            if (words == null || words.isEmpty()) continue;

            words.sort(Comparator
                    .comparing((Word w) -> Optional.ofNullable(w.getDisplayOrder()).orElse(Long.MAX_VALUE))
                    .thenComparing(Word::getId));

            long nextOrder = DISPLAY_ORDER_INITIAL;
            List<Word> updatedWords = new ArrayList<>();

            for (Word word : words) {
                if (word.getDisplayOrder() == null || !word.getDisplayOrder().equals(nextOrder)) {
                    word.setDisplayOrder(nextOrder);
                    word.setUpdatedAt(LocalDateTime.now());
                    updatedWords.add(word);
                }
                nextOrder += DISPLAY_ORDER_INCREMENT;
            }

            if (!updatedWords.isEmpty()) {
                wordRepo.saveAll(updatedWords);
                totalUpdated += updatedWords.size();
            }
        }

        return totalUpdated;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
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
        log.info("🔁 convertToResponseDTO wordId={} quizModes={}", word.getId(), word.getQuizModes());
        WordResponseDTO responseDTO = new WordResponseDTO();
        responseDTO.setId(word.getId());
        responseDTO.setWord(word.getWord());
        responseDTO.setWordType(word.getWordType());
        responseDTO.setExpandedForm(word.getExpandedForm());
        responseDTO.setPartOfSpeech(word.getPartOfSpeech());
        responseDTO.setCategoryId(word.getCategory() != null ? word.getCategory().getId() : null);
        responseDTO.setCategoryName(word.getCategory() != null ? word.getCategory().getName() : null);
        responseDTO.setMeaning(word.getMeaning());
        List<String> imgs = word.getImages();
        responseDTO.setImages(toImageUrls(imgs));
        responseDTO.setVideos(toVideoUrls(word.getVideos()));
        responseDTO.setAudios(toAudioUrls(word.getAudios()));
        responseDTO.setFacts(parseFacts(word.getFactsJson()));
        responseDTO.setExamples(parseExamples(word.getExamplesJson()));
        responseDTO.setCreated(formatDateTime(word.getCreatedAt()));
        responseDTO.setUpdated(formatDateTime(word.getUpdatedAt()));
        responseDTO.setDescription(word.getDescription());
        responseDTO.setSourceAndCredits(parseSourceCredits(word.getSourceCreditsJson()));
        responseDTO.setDisplayOrder(word.getDisplayOrder());

        responseDTO.setQuizModes(mapQuizModes(word.getQuizModes()));

        // related words - read persisted related IDs and resolve titles when possible
        List<Long> relatedIds = word.getRelatedWordIds();
        if (relatedIds == null || relatedIds.isEmpty()) {
            responseDTO.setRelatedWordIds(Collections.emptyList());
        } else {
            List<WordResponseDTO.RelatedWord> related = new ArrayList<>();
            for (Long rid : relatedIds) {
                wordRepo.findById(rid).ifPresentOrElse(
                        rw -> related.add(new WordResponseDTO.RelatedWord(rw.getId(), rw.getWord())),
                        () -> related.add(new WordResponseDTO.RelatedWord(rid, null))
                );
            }
            responseDTO.setRelatedWordIds(related);
        }

        // alsoAppearsIn
        responseDTO.setAlsoAppearsIn(parseAlsoAppearsIn(word.getAlsoAppearsInJson()));

        log.info("🔁 convertToResponseDTO returning quizModes={} for wordId={}", responseDTO.getQuizModes(), word.getId());
        return responseDTO;
    }

    private List<String> mapQuizModes(Set<com.example.WordGame.modules.words.QuizMode> quizModes) {
        if (quizModes == null || quizModes.isEmpty()) {
            return Collections.emptyList();
        }
        return quizModes.stream()
                .filter(Objects::nonNull)
                .map(Enum::name)
                .sorted()
                .toList();
    }

    private List<String> resolveImagesJson(WordRequestDTO request, String resourceName, Long resourceId) {
        List<String> urls = new ArrayList<>();
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            request.getImages().stream()
                    .map(ImageUrlDTO::getImageUrl)
                    .filter(Objects::nonNull)
                    .forEach(urls::add);
        }
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            urls.addAll(request.getImageUrls());
        }

        if (request.getWordImages() != null && !request.getWordImages().isEmpty()) {
            int mediaNumber = 1;
            for (org.springframework.web.multipart.MultipartFile mf : request.getWordImages()) {
                if (mf != null && !mf.isEmpty()) {
                    try {
                        String u = imageUploadService.uploadMedia(mf, "word", "image/", resourceName, resourceId, mediaNumber++);
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

    private List<String> resolveVideoUrls(WordRequestDTO request, String resourceName, Long resourceId) {
        List<String> urls = new ArrayList<>();
        if (request.getVideos() != null) {
            request.getVideos().stream()
                .map(VideoUrlDTO::getVideoUrl)
                .filter(Objects::nonNull)
                .forEach(urls::add);
        }
        uploadMediaFiles(request.getWordVideos(), "word", "video/", resourceName, resourceId, urls);
        return urls;
    }

    private List<String> resolveAudioUrls(WordRequestDTO request, String resourceName, Long resourceId) {
        List<String> urls = new ArrayList<>();
        if (request.getAudios() != null) {
            request.getAudios().stream()
                .map(AudioUrlDTO::getAudioUrl)
                .filter(Objects::nonNull)
                .forEach(urls::add);
        }
        uploadMediaFiles(request.getWordAudios(), "word", "audio/", resourceName, resourceId, urls);
        return urls;
    }

    private void uploadMediaFiles(List<org.springframework.web.multipart.MultipartFile> files,
                                  String folder, String mediaType, String resourceName,
                                  Long resourceId, List<String> urls) {
        if (files == null) return;
        int mediaNumber = 1;
        for (org.springframework.web.multipart.MultipartFile file : files) {
            if (file != null && !file.isEmpty()) {
                try {
                    urls.add(imageUploadService.uploadMedia(file, folder, mediaType,
                            resourceName, resourceId, mediaNumber++));
                } catch (Exception e) {
                    throw new ApiException("Failed to upload " + mediaType + " media: " + e.getMessage());
                }
            }
        }
    }

    private void deleteMediaFiles(List<String> urls) {
        for (String url : urls) {
            if (url != null && !url.isBlank()) {
                try {
                    imageUploadService.deleteImage(url);
                } catch (Exception ignored) {
                }
            }
        }
    }

    private List<ImageUrlDTO> toImageUrls(List<String> urls) {
        return urls.stream().map(ImageUrlDTO::new).collect(Collectors.toList());
    }

    private List<VideoUrlDTO> toVideoUrls(List<String> urls) {
        return urls.stream().map(VideoUrlDTO::new).collect(Collectors.toList());
    }

    private List<AudioUrlDTO> toAudioUrls(List<String> urls) {
        return urls.stream().map(AudioUrlDTO::new).collect(Collectors.toList());
    }

    private Category resolveCategory(String categoryKey) {
        if (categoryKey == null || categoryKey.isBlank()) {
            throw new ApiException("Category is required");
        }

        String trimmedKey = categoryKey.trim();
        try {
            Long categoryId = Long.valueOf(trimmedKey);
            return categoryRepo.findById(categoryId)
                    .orElseGet(() -> categoryRepo.findByName(trimmedKey)
                            .orElseThrow(() -> new ApiException("Category not found with id: " + categoryId)));
        } catch (NumberFormatException ignored) {
            return categoryRepo.findByName(trimmedKey)
                    .orElseThrow(() -> new ApiException("Category not found: " + trimmedKey));
        }
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
                    .map(m -> {
                        Long cid = m.getOrDefault("categoryId", null);
                        Long wid = m.getOrDefault("wordId", null);
                        String cname = null;
                        String wname = null;
                        if (cid != null) {
                            cname = categoryRepo.findById(cid).map(cat -> cat.getName()).orElse(null);
                        }
                        if (wid != null) {
                            wname = wordRepo.findById(wid).map(Word::getWord).orElse(null);
                        }
                        return new WordResponseDTO.AlsoAppearsIn(cid, wid, cname, wname);
                    })
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