package com.example.WordGame.modules.language.controller;

import com.example.WordGame.modules.language.DTO.LanguageRequestDTO;
import com.example.WordGame.modules.language.DTO.LanguageResponseDTO;
import com.example.WordGame.modules.language.DTO.LanguageUpdateDTO;
import com.example.WordGame.modules.language.service.LanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/admin/languages", produces = "application/json")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class LanguageController {

    private final LanguageService languageService;

    @GetMapping
    public ResponseEntity<List<LanguageResponseDTO>> getAllLanguages() {
        return ResponseEntity.ok(languageService.getAllLanguages());
    }

    @GetMapping("/{id}")
    public ResponseEntity<LanguageResponseDTO> getLanguageById(@PathVariable Long id) {
        return ResponseEntity.ok(languageService.getLanguageById(id));
    }

    @PostMapping
    public ResponseEntity<LanguageResponseDTO> createLanguage(@RequestBody LanguageRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(languageService.createLanguage(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<LanguageResponseDTO> patchLanguage(@PathVariable Long id,
                                                           @RequestBody LanguageUpdateDTO request) {
        return ResponseEntity.ok(languageService.patchLanguage(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteLanguage(@PathVariable Long id) {
        languageService.deleteLanguage(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Language deleted successfully");
        return ResponseEntity.ok(response);
    }
}
