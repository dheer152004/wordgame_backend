package com.example.WordGame.Service;

import com.example.WordGame.DTO.SavedWordDTO.SaveWordRequestDTO;
import com.example.WordGame.DTO.SavedWordDTO.SavedWordCountDTO;
import com.example.WordGame.DTO.SavedWordDTO.SavedWordResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SavedWordService {

    // 1. Save a word (POST)
    SavedWordResponseDTO saveWord(String userEmail, SaveWordRequestDTO request);

    // 2. Get all saved words for a user (GET)
    Page<SavedWordResponseDTO> getSavedWords(String userEmail, Pageable pageable);

    // 3. Delete a saved word (DELETE)
    void deleteSavedWord(String userEmail, Long wordId);

    // Check if a word is saved by user
    boolean isWordSaved(String userEmail, Long wordId);

    // Get total saved words count
    SavedWordCountDTO getSavedWordsCount(String userEmail);
}