package com.example.WordGame.modules.genre.controller;

import com.example.WordGame.modules.genre.GenreDTO.GenreRequestDTO;
import com.example.WordGame.modules.genre.GenreDTO.GenreResponseDTO;
import com.example.WordGame.modules.genre.GenreDTO.GenreUpdateDTO;
import com.example.WordGame.modules.genre.service.GenreService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/genres", produces = "application/json")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class GenreController {

    private final GenreService genreService;

    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllGenres() {
        List<GenreResponseDTO> genres = genreService.getAllGenres();
        Map<String, Object> response = new HashMap<>();
        response.put("total", genres.size());
        response.put("genres", genres);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<GenreResponseDTO> getGenreById(@PathVariable Long id) {
        return ResponseEntity.ok(genreService.getGenreById(id));
    }

    @PostMapping
    public ResponseEntity<GenreResponseDTO> createGenre(@RequestBody GenreRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(genreService.createGenre(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenreResponseDTO> updateGenre(
            @PathVariable Long id,
            @RequestBody GenreUpdateDTO request) {
        return ResponseEntity.ok(genreService.updateGenre(id, request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<GenreResponseDTO> patchGenre(
            @PathVariable Long id,
            @RequestBody GenreUpdateDTO request) {
        return ResponseEntity.ok(genreService.patchGenre(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteGenre(@PathVariable Long id) {
        genreService.deleteGenre(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Genre deleted successfully");
        return ResponseEntity.ok(response);
    }
}