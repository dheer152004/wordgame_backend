package com.example.WordGame.Service.Impl;

import com.example.WordGame.DTO.Word.WordDetailResponseDTO;
import com.example.WordGame.DTO.Word.WordResponseDTO;
import com.example.WordGame.Entities.Category;
import com.example.WordGame.Entities.Word;
import com.example.WordGame.Entities.WordExample;
import com.example.WordGame.Repository.CategoryRepo;
import com.example.WordGame.Repository.WordExampleRepo;
import com.example.WordGame.Repository.WordRepo;
import com.example.WordGame.Service.WordService;
import com.example.WordGame.exceptions.ResourceNotFoundExecption;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
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
    private final ModelMapper modelMapper;

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

        // Get all examples for this word
        List<String> examples = wordExampleRepo.findByWord(word).stream()
                .map(WordExample::getExample)
                .collect(Collectors.toList());

        // Convert Word to WordDetailResponseDTO using ModelMapper
        WordDetailResponseDTO responseDTO = modelMapper.map(word, WordDetailResponseDTO.class);
        responseDTO.setCategoryName(word.getCategory().getName());
        responseDTO.setExamples(examples);

        return responseDTO;
    }

    // Convert Word to DTO for swipe cards using ModelMapper
    private WordResponseDTO convertToResponseDTO(Word word) {
        WordResponseDTO responseDTO = modelMapper.map(word, WordResponseDTO.class);
        responseDTO.setCategoryName(word.getCategory().getName());
        return responseDTO;
    }
}