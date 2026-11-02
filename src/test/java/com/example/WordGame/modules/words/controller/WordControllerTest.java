package com.example.WordGame.modules.words.controller;

import com.example.WordGame.modules.words.DTO.WordResponseDTO;
import com.example.WordGame.modules.words.service.WordService;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WordControllerTest {

    @Test
    void randomWordsStartsNewSequenceAndReturnsToken() {
        WordService wordService = mock(WordService.class);
        WordController controller = new WordController(wordService);
        WordResponseDTO word = new WordResponseDTO();
        Page<WordResponseDTO> wordsPage = new PageImpl<>(List.of(word), Pageable.ofSize(10), 21);
        when(wordService.getRandomWords(anyString(), any(Pageable.class))).thenReturn(wordsPage);

        ResponseEntity<Map<String, Object>> response = controller.getRandomWords(0, 10, null);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(List.of(word), body.get("words"));
        assertEquals(0, body.get("currentPage"));
        assertEquals(3, body.get("totalPages"));
        assertEquals(21L, body.get("totalWords"));
        assertEquals(10, body.get("pageSize"));
        assertEquals(true, body.get("isRandom"));
        assertNotNull(body.get("paginationToken"));
        verify(wordService).getRandomWords((String) body.get("paginationToken"), Pageable.ofSize(10));
    }

    @Test
    void randomWordsRequiresTokenForLaterPages() {
        WordController controller = new WordController(mock(WordService.class));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> controller.getRandomWords(1, 10, null));

        assertEquals(400, exception.getStatusCode().value());
    }
}
