package com.example.WordGame.modules.roles.editor;

import com.example.WordGame.modules.words.DTO.*;
import com.example.WordGame.modules.words.service.WordService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/editor/words", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class EditorWordController {

    private final WordService wordService;

    @PostMapping
    public ResponseEntity<WordResponseDTO> createWordJson(@RequestBody WordRequestDTO request) {
        log.info("[Editor] createWordJson received quizModes: {}", request.getQuizModes());
        WordResponseDTO word = wordService.createWord(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(word);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WordResponseDTO> updateWord(@PathVariable Long id, @RequestBody WordRequestDTO request) {
        WordResponseDTO word = wordService.updateWord(id, request);
        return ResponseEntity.ok(word);
    }
}
