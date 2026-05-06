package com.example.WordGame.Service.Impl;

import com.example.WordGame.DTO.WordDetailResponseDTO;
import com.example.WordGame.DTO.WordResponseDTO;
import com.example.WordGame.Entities.Category;
import com.example.WordGame.Entities.Word;
import com.example.WordGame.Entities.WordExample;
import com.example.WordGame.Repository.CategoryRepo;
import com.example.WordGame.Repository.WordExampleRepo;
import com.example.WordGame.Repository.WordRepo;
import com.example.WordGame.Service.WordService;
import com.example.WordGame.exceptions.ResourceNotFoundExecption;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WordServiceImpl implements WordService {

    private final WordRepo wordRepo;
    private final CategoryRepo categoryRepo;
    private final WordExampleRepo wordExampleRepo;

    @Override
    public Page<WordResponseDTO> getWordsByCategory(String categoryName, Pageable pageable) {
        Category category = categoryRepo.findByName(categoryName)
                .orElseThrow(() -> new RuntimeException("Category not found: " + categoryName));

        return wordRepo.findByCategory(category, pageable)
                .map(this::convertToResponseDTO);
    }

    @Override
    public WordDetailResponseDTO getWordDetail(Long wordId) {
        // ✅ FIXED: Added () -> before the exception
        Word word = (Word) wordRepo.findById(wordId)
                .orElseThrow(() -> new ResourceNotFoundExecption("word", "wordId", wordId));

        // Get all examples for this word
        List<String> examples = wordExampleRepo.findByWord(word).stream()
                .map(WordExample::getExample)
                .collect(Collectors.toList());

        return new WordDetailResponseDTO(
                word.getId(),
                word.getWord(),
                word.getMeaning(),
                word.getMemeImageUrl(),
                word.getCategory().getName(),
                examples
        );
    }

    // Convert Word to DTO for swipe cards
    private WordResponseDTO convertToResponseDTO(Word word) {
        return new WordResponseDTO(
                word.getId(),
                word.getWord(),
                word.getMeaning(),
                word.getMemeImageUrl(),
                word.getCategory().getName()
        );
    }
}