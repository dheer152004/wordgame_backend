package com.example.WordGame.Controller;

import com.example.WordGame.DTO.Word.*;
import com.example.WordGame.Service.WordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/words")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminWordController {

    private final WordService wordService;

    @GetMapping("/{id}")
    public ResponseEntity<WordResponseDTO> getWordById(@PathVariable Long id) {
        return ResponseEntity.ok(wordService.getWordById(id));
    }

    @PostMapping
    public ResponseEntity<WordResponseDTO> createWord(
            @ModelAttribute WordRequestDTO request) {
        WordResponseDTO word = wordService.createWord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(word);
    }

    @PostMapping("/bulk/{categoryId}")
    public ResponseEntity<Map<String, Object>> bulkCreateWords(
            @PathVariable Long categoryId,
            @RequestBody BulkWordImportDTO bulkRequest) {
        List<WordResponseDTO> words = wordService.bulkCreateWords(categoryId, bulkRequest);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Successfully imported " + words.size() + " words");
        response.put("words", words);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WordResponseDTO> updateWord(
            @PathVariable Long id,
            @ModelAttribute WordRequestDTO request) {
        WordResponseDTO word = wordService.updateWord(id, request);
        return ResponseEntity.ok(word);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteWord(@PathVariable Long id) {
        wordService.deleteWord(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Word deleted successfully");
        return ResponseEntity.ok(response);
    }
}