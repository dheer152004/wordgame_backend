package com.example.WordGame.modules.words.service;

import com.example.WordGame.Service.ImageStorageService;
import com.example.WordGame.modules.GrammarCategories.entity.GrammarCategory;
import com.example.WordGame.modules.GrammarValues.entity.GrammarValue;
import com.example.WordGame.modules.category.Entities.Category;
import com.example.WordGame.modules.category.repository.CategoryRepo;
import com.example.WordGame.modules.language.entity.Language;
import com.example.WordGame.modules.words.DTO.WordResponseDTO;
import com.example.WordGame.modules.words.Entities.Word;
import com.example.WordGame.modules.words.repository.WordRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.LongStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyIterable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WordServiceImplRandomPaginationTest {

    @Test
    void randomPagesDoNotRepeatWordsAndNewSessionGetsNewOrder() {
        WordRepo wordRepo = mock(WordRepo.class);
        when(wordRepo.findAllWordIds()).thenReturn(LongStream.rangeClosed(1, 25).boxed().toList());
        when(wordRepo.findAllById(anyIterable())).thenAnswer(invocation -> {
            Iterable<Long> ids = invocation.getArgument(0);
            List<Word> words = new ArrayList<>();
            for (Long id : ids) {
                words.add(createWord(id));
            }
            return words;
        });

        WordServiceImpl service = new WordServiceImpl(
                wordRepo,
                mock(CategoryRepo.class),
                new ModelMapper(),
                mock(ImageStorageService.class),
                new ObjectMapper());
        ReflectionTestUtils.setField(service, "cacheManager", new ConcurrentMapCacheManager("randomWords"));

        String firstToken = UUID.randomUUID().toString();
        Page<WordResponseDTO> firstPage = service.getRandomWords(firstToken, PageRequest.of(0, 10));
        Page<WordResponseDTO> secondPage = service.getRandomWords(firstToken, PageRequest.of(1, 10));
        Page<WordResponseDTO> thirdPage = service.getRandomWords(firstToken, PageRequest.of(2, 10));

        List<Long> firstSequence = new ArrayList<>();
        firstSequence.addAll(ids(firstPage));
        firstSequence.addAll(ids(secondPage));
        firstSequence.addAll(ids(thirdPage));
        assertEquals(25, firstSequence.size());
        assertEquals(25, firstSequence.stream().distinct().count());
        assertEquals(3, firstPage.getTotalPages());
        assertFalse(thirdPage.hasNext());

        String secondToken = UUID.randomUUID().toString();
        Page<WordResponseDTO> newSessionFirstPage =
                service.getRandomWords(secondToken, PageRequest.of(0, 10));
        assertNotEquals(ids(firstPage), ids(newSessionFirstPage));
    }

    @Test
    void randomPageSizeMustRemainTheSameWithinSession() {
        WordRepo wordRepo = mock(WordRepo.class);
        when(wordRepo.findAllWordIds()).thenReturn(LongStream.rangeClosed(1, 12).boxed().toList());
        when(wordRepo.findAllById(anyIterable())).thenAnswer(invocation -> {
            Iterable<Long> ids = invocation.getArgument(0);
            List<Word> words = new ArrayList<>();
            for (Long id : ids) {
                words.add(createWord(id));
            }
            return words;
        });

        WordServiceImpl service = new WordServiceImpl(
                wordRepo,
                mock(CategoryRepo.class),
                new ModelMapper(),
                mock(ImageStorageService.class),
                new ObjectMapper());
        ReflectionTestUtils.setField(service, "cacheManager", new ConcurrentMapCacheManager("randomWords"));
        String token = UUID.randomUUID().toString();
        service.getRandomWords(token, PageRequest.of(0, 10));

        assertThrows(org.springframework.web.server.ResponseStatusException.class,
                () -> service.getRandomWords(token, PageRequest.of(1, 5)));
    }

    private static List<Long> ids(Page<WordResponseDTO> page) {
        return page.getContent().stream().map(WordResponseDTO::getId).toList();
    }

    private static Word createWord(Long id) {
        Category category = new Category();
        category.setId(1L);
        category.setName("Category");

        GrammarCategory grammarCategory = new GrammarCategory();
        grammarCategory.setName("Part of speech");
        GrammarValue grammarValue = new GrammarValue();
        grammarValue.setGrammarCategory(grammarCategory);
        grammarValue.setName("Noun");

        Word word = new Word();
        word.setId(id);
        word.setWord("word-" + id);
        word.setLanguage(new Language(1L, "en", "English", null, null, true, null, true));
        word.setCategories(new LinkedHashSet<>(Set.of(category)));
        word.setGrammarValues(new LinkedHashSet<>(Set.of(grammarValue)));
        return word;
    }
}
