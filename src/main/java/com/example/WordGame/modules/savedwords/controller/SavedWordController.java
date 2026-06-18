package com.example.WordGame.modules.savedwords.controller;

import com.example.WordGame.modules.savedwords.SavedWordDTO.SaveWordRequestDTO;
import com.example.WordGame.modules.savedwords.SavedWordDTO.SavedWordCountDTO;
import com.example.WordGame.modules.savedwords.SavedWordDTO.SavedWordResponseDTO;
import com.example.WordGame.modules.savedwords.service.SavedWordService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/saved-words")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SavedWordController {

    private final SavedWordService savedWordService;

    /**
     * 1. POST /api/user/saved-words
     * Save a word to user's collection
     */
    @PostMapping
    public ResponseEntity<SavedWordResponseDTO> saveWord(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SaveWordRequestDTO request) {
        String userEmail = userDetails.getUsername();
        SavedWordResponseDTO response = savedWordService.saveWord(userEmail, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 2. GET /api/user/saved-words?page=0&size=10
     * Get all saved words for the authenticated user
     */
    @GetMapping
    public ResponseEntity<Map<String, Object>> getSavedWords(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        String userEmail = userDetails.getUsername();
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "savedAt"));
        Page<SavedWordResponseDTO> savedWordsPage = savedWordService.getSavedWords(userEmail, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("content", savedWordsPage.getContent());
        response.put("currentPage", savedWordsPage.getNumber());
        response.put("totalItems", savedWordsPage.getTotalElements());
        response.put("totalPages", savedWordsPage.getTotalPages());
        response.put("hasNext", savedWordsPage.hasNext());
        response.put("hasPrevious", savedWordsPage.hasPrevious());

        return ResponseEntity.ok(response);
    }

    /**
     * 3. DELETE /api/user/saved-words/{wordId}
     * Remove a word from user's saved collection
     */
    @DeleteMapping("/{wordId}")
    public ResponseEntity<Map<String, String>> deleteSavedWord(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long wordId) {

        String userEmail = userDetails.getUsername();
        savedWordService.deleteSavedWord(userEmail, wordId);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Word removed from saved collection successfully");
        response.put("wordId", String.valueOf(wordId));

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/user/saved-words/check/{wordId}
     * Check if a specific word is saved by the user
     */
    @GetMapping("/check/{wordId}")
    public ResponseEntity<Map<String, Boolean>> checkWordSaved(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long wordId) {

        String userEmail = userDetails.getUsername();
        boolean isSaved = savedWordService.isWordSaved(userEmail, wordId);

        Map<String, Boolean> response = new HashMap<>();
        response.put("saved", isSaved);

        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/user/saved-words/count
     * Get total count of saved words for the user
     */
    @GetMapping("/count")
    public ResponseEntity<SavedWordCountDTO> getSavedWordsCount(
            @AuthenticationPrincipal UserDetails userDetails) {

        String userEmail = userDetails.getUsername();
        SavedWordCountDTO count = savedWordService.getSavedWordsCount(userEmail);

        return ResponseEntity.ok(count);
    }
}