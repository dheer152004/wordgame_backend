package com.example.WordGame.Controller;

import com.example.WordGame.DTO.GenreDTO.GenreRequestDTO;
import com.example.WordGame.DTO.GenreDTO.GenreResponseDTO;
import com.example.WordGame.DTO.GenreDTO.GenreUpdateDTO;
import com.example.WordGame.Service.GenreService;
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
    public ResponseEntity<List<GenreResponseDTO>> getAllGenres() {
        return ResponseEntity.ok(genreService.getAllGenres());
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