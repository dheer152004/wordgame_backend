package com.example.WordGame.modules.GrammarValues.controller;

import com.example.WordGame.modules.GrammarValues.DTO.GrammarValueRequestDTO;
import com.example.WordGame.modules.GrammarValues.DTO.GrammarValueResponseDTO;
import com.example.WordGame.modules.GrammarValues.DTO.GrammarValueUpdateDTO;
import com.example.WordGame.modules.GrammarValues.service.GrammarValueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/admin/grammar-values", produces = "application/json")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GrammarValueController {

    private final GrammarValueService grammarValueService;

    @GetMapping
    public ResponseEntity<List<GrammarValueResponseDTO>> getAllGrammarValues() {
        return ResponseEntity.ok(grammarValueService.getAllGrammarValues());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrammarValueResponseDTO> getGrammarValueById(@PathVariable Long id) {
        return ResponseEntity.ok(grammarValueService.getGrammarValueById(id));
    }

    @PostMapping
    public ResponseEntity<GrammarValueResponseDTO> createGrammarValue(@RequestBody GrammarValueRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(grammarValueService.createGrammarValue(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GrammarValueResponseDTO> patchGrammarValue(@PathVariable Long id,
                                                                   @RequestBody GrammarValueUpdateDTO request) {
        return ResponseEntity.ok(grammarValueService.patchGrammarValue(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGrammarValue(@PathVariable Long id) {
        grammarValueService.deleteGrammarValue(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Grammar value deleted successfully");
        return ResponseEntity.ok(response);
    }
}
