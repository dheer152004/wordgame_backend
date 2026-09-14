package com.example.WordGame.modules.words.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.WordGame.modules.words.DTO.WordDetailResponseDTO;
import com.example.WordGame.modules.words.DTO.WordRequestDTO;
import com.example.WordGame.modules.words.DTO.WordResponseDTO;

// import java.util.List;

public interface WordService {

    // Existing methods...
    Page<WordResponseDTO> getWordsByCategory(String categoryName, Pageable pageable);

    Page<WordResponseDTO> getWordsByCategory(String categoryName, Pageable pageable, String userEmail);
    WordDetailResponseDTO getWordDetail(Long wordId);
    WordResponseDTO getWordById(Long id);
    WordResponseDTO createWord(WordRequestDTO request);
    WordResponseDTO updateWord(Long id, WordRequestDTO request);
    WordResponseDTO updateWordDisplayOrder(Long id, Long displayOrder);
    WordResponseDTO updateWordDisplayOrder(Long id, Long categoryId, Long displayOrder);
    int rebalanceDisplayOrder();
    void deleteWord(Long id);
    WordResponseDTO toggleWordStatus(Long id);
    WordResponseDTO getWordResponseDTO(Long id);


    // ✅ ONLY ONE NEW METHOD - Random words without category
    Page<WordResponseDTO> getRandomWords(Pageable pageable);

    // Search words by text (case-insensitive, partial match)
    Page<WordResponseDTO> searchWords(String q, Pageable pageable);

}
