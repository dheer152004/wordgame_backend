package com.example.WordGame.modules.savedwords.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.WordGame.modules.savedwords.SavedWordDTO.SaveWordRequestDTO;
import com.example.WordGame.modules.savedwords.SavedWordDTO.SavedWordCountDTO;
import com.example.WordGame.modules.savedwords.SavedWordDTO.SavedWordResponseDTO;

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