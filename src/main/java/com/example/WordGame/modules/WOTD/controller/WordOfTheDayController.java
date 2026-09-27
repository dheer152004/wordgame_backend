package com.example.WordGame.modules.WOTD.controller;

import com.example.WordGame.modules.WOTD.dto.WordOfTheDayPublicResponse;
import com.example.WordGame.modules.WOTD.dto.WordOfTheDayRequest;
import com.example.WordGame.modules.WOTD.dto.WordOfTheDayResponse;
import com.example.WordGame.modules.WOTD.service.WordOfTheDayService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/api/word-of-the-day", produces = "application/json")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class WordOfTheDayController {
    private final WordOfTheDayService service;

    @GetMapping
    public ResponseEntity<WordOfTheDayPublicResponse> getToday() {
        return ResponseEntity.ok(service.getToday());
    }

    @PostMapping(consumes = "application/json")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<WordOfTheDayResponse> create(@RequestBody WordOfTheDayRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(request));
    }
}