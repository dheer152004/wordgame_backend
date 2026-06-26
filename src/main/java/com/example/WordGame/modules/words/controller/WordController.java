package com.example.WordGame.modules.words.controller;

import com.example.WordGame.modules.words.DTO.WordDetailResponseDTO;
import com.example.WordGame.modules.words.DTO.WordResponseDTO;
import com.example.WordGame.modules.words.service.WordService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/words", produces = "application/json")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WordController {

    private final WordService wordService;

    // GET /api/words/category/{categoryName}?page=0&size=10
    @GetMapping("/category/{categoryName}")
    public ResponseEntity<Map<String, Object>> getWordsByCategory(
            @PathVariable String categoryName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<WordResponseDTO> wordsPage = wordService.getWordsByCategory(categoryName, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("words", wordsPage.getContent());
        response.put("currentPage", wordsPage.getNumber());
        response.put("totalPages", wordsPage.getTotalPages());
        response.put("totalWords", wordsPage.getTotalElements());
        response.put("hasMore", wordsPage.hasNext());
        response.put("category", categoryName);

        return ResponseEntity.ok(response);
    }

    // GET /api/words/{id}  -- only numeric ids (avoid clash with other literal paths like /search)
    @GetMapping("/{id:\\d+}")
    public ResponseEntity<WordDetailResponseDTO> getWordDetail(@PathVariable Long id) {
        WordDetailResponseDTO wordDetail = wordService.getWordDetail(id);
        return ResponseEntity.ok(wordDetail);
    }

    // ✅ NEW API - Get random words without category (WITH CACHING)
    @GetMapping("/random")
    public ResponseEntity<Map<String, Object>> getRandomWords(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<WordResponseDTO> wordsPage = wordService.getRandomWords(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("words", wordsPage.getContent());
        response.put("currentPage", wordsPage.getNumber());
        response.put("totalPages", wordsPage.getTotalPages());
        response.put("totalWords", wordsPage.getTotalElements());
        response.put("hasMore", wordsPage.hasNext());
        response.put("pageSize", wordsPage.getSize());
        response.put("isRandom", true);

        return ResponseEntity.ok(response);
    }

    // GET /api/words/search?q=bat&page=0&size=10
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchWords(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<WordResponseDTO> wordsPage = wordService.searchWords(q, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("words", wordsPage.getContent());
        response.put("currentPage", wordsPage.getNumber());
        response.put("totalPages", wordsPage.getTotalPages());
        response.put("totalWords", wordsPage.getTotalElements());
        response.put("hasMore", wordsPage.hasNext());
        response.put("query", q);

        return ResponseEntity.ok(response);
    }
}