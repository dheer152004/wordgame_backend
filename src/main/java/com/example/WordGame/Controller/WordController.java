package com.example.WordGame.Controller;

import com.example.WordGame.DTO.Word.WordResponseDTO;
import com.example.WordGame.DTO.Word.WordDetailResponseDTO;
import com.example.WordGame.Service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/words")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WordController {

    private final WordService wordService;

    // GET /api/words/category/{categoryName}?page=0&size=10
    // Get words for swiping based on selected category
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

    // GET /api/words/{id} - Get word details when user clicks
    @GetMapping("/{id}")
    public ResponseEntity<WordDetailResponseDTO> getWordDetail(@PathVariable Long id) {
        WordDetailResponseDTO wordDetail = wordService.getWordDetail(id);
        return ResponseEntity.ok(wordDetail);
    }
}