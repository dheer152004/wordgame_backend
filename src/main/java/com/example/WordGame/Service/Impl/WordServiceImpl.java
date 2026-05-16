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
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordServiceImpl implements WordService {

    private final WordRepo wordRepo;
    private final CategoryRepo categoryRepo;
    private final WordExampleRepo wordExampleRepo;
    private final ModelMapper modelMapper;
    private final CloudflareImageUploadService imageUploadService;

    // ============ EXISTING METHODS ============

    @Override
    public Page<WordResponseDTO> getWordsByCategory(String categoryName, Pageable pageable) {
        Category category = categoryRepo.findByName(categoryName)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));

        return wordRepo.findByCategory(category, pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    public WordDetailResponseDTO getWordDetail(Long wordId) {
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

    // ============ NEW ADMIN METHODS ============

    @Override
    public WordResponseDTO getWordById(Long id) {
        Word word = wordRepo.findById(id)
                .orElseThrow(() -> new ApiException("Word not found with id: " + id));
        return convertToResponseDTO(word);
    }

    @Override
    @Transactional
    public WordResponseDTO createWord(WordRequestDTO request) {
        Category category = categoryRepo.findById(request.getCategoryId())
                .orElseThrow(() -> new ApiException("Category not found with id: " + request.getCategoryId()));

        // Check if word already exists
        if (wordRepo.findByWord(request.getWord()).isPresent()) {
            throw new ApiException("Word already exists: " + request.getWord());
        }

        Word word = new Word();
        word.setWord(request.getWord());
        word.setMeaning(request.getMeaning());
        word.setCategory(category);
        word.setCreatedAt(LocalDateTime.now());

        // Upload meme image to Cloudflare R2
        if (request.getMemeImage() != null && !request.getMemeImage().isEmpty()) {
            try {
                String imageUrl = imageUploadService.uploadImage(request.getMemeImage(), "words");
                word.setMemeImageUrl(imageUrl);
            } catch (Exception e) {
                throw new ApiException("Failed to upload image: " + e.getMessage());
            }
        }

        Word savedWord = wordRepo.save(word);

        // Add examples
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

        return convertToResponseDTO(savedWord);
    }

    @Override
    @Transactional
    public WordResponseDTO updateWord(Long id, WordRequestDTO request) {
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

        // Upload new meme image to Cloudflare R2 if provided
        if (request.getMemeImage() != null && !request.getMemeImage().isEmpty()) {
            // Delete old image from Cloudflare R2
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

        // Update examples
        if (request.getExamples() != null) {
            // Delete existing examples
            List<WordExample> existingExamples = wordExampleRepo.findByWord(word);
            wordExampleRepo.deleteAll(existingExamples);

            // Add new examples
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

        return convertToResponseDTO(updatedWord);
    }

    @Override
    @Transactional
    public void deleteWord(Long id) {
        Word word = wordRepo.findById(id)
                .orElseThrow(() -> new ApiException("Word not found with id: " + id));

        // Delete examples
        List<WordExample> examples = wordExampleRepo.findByWord(word);
        wordExampleRepo.deleteAll(examples);

        // Delete meme image from Cloudflare R2
        if (word.getMemeImageUrl() != null) {
            imageUploadService.deleteImage(word.getMemeImageUrl());
        }

        wordRepo.delete(word);
    }

    @Override
    @Transactional
    public List<WordResponseDTO> bulkCreateWords(Long categoryId, BulkWordImportDTO bulkRequest) {
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new ApiException("Category not found with id: " + categoryId));

        List<WordResponseDTO> createdWords = new ArrayList<>();

        for (BulkWordImportDTO.WordEntry wordEntry : bulkRequest.getWords()) {
            // Check if word already exists
            if (wordRepo.findByWord(wordEntry.getWord()).isPresent()) {
                throw new ApiException("Word already exists: " + wordEntry.getWord());
            }

            Word word = new Word();
            word.setWord(wordEntry.getWord());
            word.setMeaning(wordEntry.getMeaning());
            word.setCategory(category);
            word.setCreatedAt(LocalDateTime.now());

            // Set image URL if provided (can be existing Cloudflare URL or null)
            if (wordEntry.getMemeImageUrl() != null && !wordEntry.getMemeImageUrl().isEmpty()) {
                word.setMemeImageUrl(wordEntry.getMemeImageUrl());
            }

            Word savedWord = wordRepo.save(word);

            // Add examples
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

        return createdWords;
    }

    @Override
    public WordResponseDTO toggleWordStatus(Long id) {
        Word word = wordRepo.findById(id)
                .orElseThrow(() -> new ApiException("Word not found with id: " + id));
        return convertToResponseDTO(word);
    }

    // ============ HELPER METHODS ============

    private WordResponseDTO convertToResponseDTO(Word word) {
        WordResponseDTO responseDTO = modelMapper.map(word, WordResponseDTO.class);
        responseDTO.setCategoryName(word.getCategory().getName());
        return responseDTO;
    }
}
