package com.example.WordGame.modules.language;

import com.example.WordGame.modules.language.DTO.LanguageRequestDTO;
import com.example.WordGame.modules.language.DTO.LanguageResponseDTO;
import com.example.WordGame.modules.language.repository.LanguageRepository;
import com.example.WordGame.modules.language.service.LanguageServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class LanguageServiceTest {

    @Test
    void shouldCreateLanguageWithProvidedValues() {
        LanguageRepository repository = Mockito.mock(LanguageRepository.class);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        LanguageServiceImpl service = new LanguageServiceImpl(repository);

        LanguageRequestDTO request = new LanguageRequestDTO();
        request.setCode("en");
        request.setName("English");
        request.setGrammarName("Grammar");
        request.setGrammarDescription("English grammar");
        request.setGrammarActive(true);
        request.setDisplayOrder(10L);
        request.setIsActive(true);

        LanguageResponseDTO response = service.createLanguage(request);

        assertEquals("en", response.getCode());
        assertEquals("English", response.getName());
        assertEquals("Grammar", response.getGrammarName());
        assertTrue(response.getGrammarActive());
        assertEquals(10L, response.getDisplayOrder());
        assertTrue(response.getIsActive());
    }
}
