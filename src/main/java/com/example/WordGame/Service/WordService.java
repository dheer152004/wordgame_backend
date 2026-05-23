package com.example.WordGame.Service;

import com.example.WordGame.DTO.Word.BulkWordImportDTO;
import com.example.WordGame.DTO.Word.WordDetailResponseDTO;
import com.example.WordGame.DTO.Word.WordRequestDTO;
import com.example.WordGame.DTO.Word.WordResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WordService {

    // Existing methods...
    Page<WordResponseDTO> getWordsByCategory(String categoryName, Pageable pageable);
    WordDetailResponseDTO getWordDetail(Long wordId);
    WordResponseDTO getWordById(Long id);
    WordResponseDTO createWord(WordRequestDTO request);
    WordResponseDTO updateWord(Long id, WordRequestDTO request);
    void deleteWord(Long id);
    List<WordResponseDTO> bulkCreateWords(Long categoryId, BulkWordImportDTO bulkRequest);
    WordResponseDTO toggleWordStatus(Long id);

    // ✅ ONLY ONE NEW METHOD - Random words without category
    Page<WordResponseDTO> getRandomWords(Pageable pageable);

}
