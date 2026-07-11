package com.example.WordGame.modules.roles.admin;

import com.example.WordGame.modules.words.DTO.*;
import com.example.WordGame.modules.words.service.WordService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/admin/words", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class AdminWordController {

    private final WordService wordService;

    @PostMapping
    public ResponseEntity<WordResponseDTO> createWord(
        @ModelAttribute WordRequestDTO request) {
        log.info("[Admin] createWord (form) received quizModes: {}", request.getQuizModes());
        WordResponseDTO word = wordService.createWord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(word);
    }
    
    
    @PostMapping(consumes = "application/json")
    public ResponseEntity<WordResponseDTO> createWordJson(
            @RequestBody WordRequestDTO request) {
                log.info("[Admin] createWordJson received quizModes: {}", request.getQuizModes());
                WordResponseDTO word = wordService.createWord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(word);
    }

    @PostMapping("/rebalance-display-order")
    public ResponseEntity<Map<String, Object>> rebalanceDisplayOrder() {
        int updatedRecords = wordService.rebalanceDisplayOrder();
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("updatedRecords", updatedRecords);
        response.put("message", updatedRecords == 0 ? "No words found." : "Display order rebalanced successfully.");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WordResponseDTO> getWordById(@PathVariable Long id) {
        return ResponseEntity.ok(wordService.getWordById(id));
    }

    @GetMapping("/{id}/display-order")
    public ResponseEntity<Map<String, Object>> getDisplayOrder(@PathVariable Long id) {
        WordResponseDTO word = wordService.getWordById(id);
        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("displayOrder", word.getDisplayOrder());
        return ResponseEntity.ok(response);
    }
    
    // @PostMapping("/bulk/{categoryId}")
    // public ResponseEntity<Map<String, Object>> bulkCreateWords(
    //         @PathVariable Long categoryId,
    //         @RequestBody BulkWordImportDTO bulkRequest) {
    //     List<WordResponseDTO> words = wordService.bulkCreateWords(categoryId, bulkRequest);

    //     Map<String, Object> response = new HashMap<>();
    //     response.put("message", "Successfully imported " + words.size() + " words");
    //     response.put("words", words);
    //     return ResponseEntity.ok(response);
    // }

    @PutMapping("/{id}")
    public ResponseEntity<WordResponseDTO> updateWord(
            @PathVariable Long id,
            @ModelAttribute WordRequestDTO request) {
        WordResponseDTO word = wordService.updateWord(id, request);
        return ResponseEntity.ok(word);
    }

    @PatchMapping(path = "/{id}", consumes = "application/json")
    public ResponseEntity<WordResponseDTO> patchWordJson(
            @PathVariable Long id,
            @RequestBody WordRequestDTO request) {
        WordResponseDTO word = wordService.updateWord(id, request);
        return ResponseEntity.ok(word);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<WordResponseDTO> patchWord(
            @PathVariable Long id,
            @ModelAttribute WordRequestDTO request) {
        WordResponseDTO word = wordService.updateWord(id, request);
        return ResponseEntity.ok(word);
    }

    @PatchMapping(path = "/{id}/display-order", consumes = "application/json")
    public ResponseEntity<WordResponseDTO> updateDisplayOrder(
            @PathVariable Long id,
            @RequestBody WordRequestDTO request) {
        WordResponseDTO word = wordService.updateWordDisplayOrder(id, request.getDisplayOrder());
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