package com.example.WordGame.modules.GrammarCategories.controller;

import com.example.WordGame.modules.GrammarCategories.DTO.GrammarCategoryRequestDTO;
import com.example.WordGame.modules.GrammarCategories.DTO.GrammarCategoryResponseDTO;
import com.example.WordGame.modules.GrammarCategories.DTO.GrammarCategoryUpdateDTO;
import com.example.WordGame.modules.GrammarCategories.service.GrammarCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/admin/grammar-categories", produces = "application/json")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GrammarCategoryController {

    private final GrammarCategoryService grammarCategoryService;

    @GetMapping
    public ResponseEntity<List<GrammarCategoryResponseDTO>> getAllGrammarCategories() {
        return ResponseEntity.ok(grammarCategoryService.getAllGrammarCategories());
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrammarCategoryResponseDTO> getGrammarCategoryById(@PathVariable Long id) {
        return ResponseEntity.ok(grammarCategoryService.getGrammarCategoryById(id));
    }

    @PostMapping
    public ResponseEntity<GrammarCategoryResponseDTO> createGrammarCategory(@RequestBody GrammarCategoryRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(grammarCategoryService.createGrammarCategory(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GrammarCategoryResponseDTO> patchGrammarCategory(@PathVariable Long id,
                                                                         @RequestBody GrammarCategoryUpdateDTO request) {
        return ResponseEntity.ok(grammarCategoryService.patchGrammarCategory(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGrammarCategory(@PathVariable Long id) {
        grammarCategoryService.deleteGrammarCategory(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Grammar category deleted successfully");
        return ResponseEntity.ok(response);
    }
}
