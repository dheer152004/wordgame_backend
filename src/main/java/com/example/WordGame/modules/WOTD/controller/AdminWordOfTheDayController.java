package com.example.WordGame.modules.WOTD.controller;

import com.example.WordGame.modules.WOTD.dto.WordOfTheDayRequest;
import com.example.WordGame.modules.WOTD.dto.WordOfTheDayResponse;
import com.example.WordGame.modules.WOTD.service.WordOfTheDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping(value = "/api/admin/word-of-the-day", produces = "application/json")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@PreAuthorize("hasRole('ADMIN')")
public class AdminWordOfTheDayController {
    private final WordOfTheDayService service;

    @PostMapping(consumes = "application/json")
    public ResponseEntity<WordOfTheDayResponse> create(@RequestBody WordOfTheDayRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }

    @PatchMapping(path = "/{id}", consumes = "application/json")
    public ResponseEntity<WordOfTheDayResponse> update(
            @PathVariable("id") Long wordOfTheDayId, @RequestBody WordOfTheDayRequest request) {
        return ResponseEntity.ok(service.update(wordOfTheDayId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long wordOfTheDayId) {
        service.delete(wordOfTheDayId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<WordOfTheDayResponse>> findBetween(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return ResponseEntity.ok(service.findBetween(from, to));
    }
}