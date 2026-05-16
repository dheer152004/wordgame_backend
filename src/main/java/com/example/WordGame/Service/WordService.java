package com.example.WordGame.Service;

import com.example.WordGame.DTO.Word.BulkWordImportDTO;
import com.example.WordGame.DTO.Word.WordDetailResponseDTO;
import com.example.WordGame.DTO.Word.WordRequestDTO;
import com.example.WordGame.DTO.Word.WordResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface WordService {

    public Page<WordResponseDTO> getWordsByCategory(String categoryName, Pageable pageable);
    public WordDetailResponseDTO getWordDetail(Long wordId);
    public WordResponseDTO toggleWordStatus(Long id);
    public List<WordResponseDTO> bulkCreateWords(Long categoryId, BulkWordImportDTO bulkRequest);
    public void deleteWord(Long id);
    public WordResponseDTO updateWord(Long id, WordRequestDTO request);
    public WordResponseDTO createWord(WordRequestDTO request);
    public WordResponseDTO getWordById(Long id);

}
