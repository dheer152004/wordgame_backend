package com.example.WordGame.Service;

import com.example.WordGame.DTO.WordDetailResponseDTO;
import com.example.WordGame.DTO.WordResponseDTO;
import com.example.WordGame.Entities.Word;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WordService {

    public Page<WordResponseDTO> getWordsByCategory(String categoryName, Pageable pageable);
    public WordDetailResponseDTO getWordDetail(Long wordId);

}
