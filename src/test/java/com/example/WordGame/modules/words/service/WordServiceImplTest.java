package com.example.WordGame.modules.words.service;

import com.example.WordGame.Service.ImageStorageService;
import com.example.WordGame.modules.category.Entities.Category;
import com.example.WordGame.modules.category.repository.CategoryRepo;
import com.example.WordGame.modules.words.DTO.WordDetailResponseDTO;
import com.example.WordGame.modules.words.DTO.WordCategoryDTO;
import com.example.WordGame.modules.words.DTO.WordRequestDTO;
import com.example.WordGame.modules.words.DTO.WordResponseDTO;
import com.example.WordGame.modules.words.Entities.Word;
import com.example.WordGame.modules.words.QuizMode;
import com.example.WordGame.modules.words.repository.WordRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.LinkedHashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class WordServiceImplTest {

    @Test
    void createWordShouldPopulateQuizModesAndDisplayOrderInResponse() {
        Category category = new Category();
        category.setId(1L);
        category.setName("TOTOTP");

        WordRequestDTO request = new WordRequestDTO();
        request.setWord("serendipity");
        request.setMeaning("A pleasant surprise");
        request.setCategories(List.of(new WordCategoryDTO(1L, null, null)));
        request.setQuizModes(List.of("IMAGE", "TEXT"));

        Word savedWord = new Word();
        savedWord.setId(1L);
        savedWord.setWord("serendipity");
        savedWord.setMeaning("A pleasant surprise");
        savedWord.setCategories(new LinkedHashSet<>(Set.of(category)));
        savedWord.setQuizModes(Set.of(QuizMode.IMAGE, QuizMode.TEXT));
        savedWord.setCategoryDisplayOrder(1L, 10000L);

        WordRepo wordRepo = (WordRepo) Proxy.newProxyInstance(
                WordRepo.class.getClassLoader(),
                new Class<?>[]{WordRepo.class},
                (proxy, method, args) -> {
                    if ("save".equals(method.getName())) {
                        return savedWord;
                    }
                    if ("findById".equals(method.getName())) {
                        return Optional.of(savedWord);
                    }
                    if ("findAllByCategoriesContaining".equals(method.getName())) {
                        return List.of();
                    }
                    if (method.getReturnType().equals(Optional.class)) {
                        return Optional.empty();
                    }
                    if (method.getReturnType().equals(boolean.class)) {
                        return false;
                    }
                    if (method.getReturnType().equals(int.class)) {
                        return 0;
                    }
                    return null;
                }
        );

        CategoryRepo categoryRepo = (CategoryRepo) Proxy.newProxyInstance(
                CategoryRepo.class.getClassLoader(),
                new Class<?>[]{CategoryRepo.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        return Optional.of(category);
                    }
                    if (method.getReturnType().equals(Optional.class)) {
                        return Optional.empty();
                    }
                    if (method.getReturnType().equals(boolean.class)) {
                        return false;
                    }
                    return null;
                }
        );

        WordServiceImpl wordService = new WordServiceImpl(
                wordRepo,
                categoryRepo,
                new ModelMapper(),
                mock(ImageStorageService.class),
                new ObjectMapper()
        );

        WordResponseDTO response = wordService.createWord(request);

        assertNotNull(response);
        assertEquals(List.of("IMAGE", "TEXT"), response.getQuizModes());
        assertEquals(1, response.getCategories().size());
        assertEquals(1L, response.getCategories().get(0).getCategoryId());
        assertEquals("TOTOTP", response.getCategories().get(0).getCategoryName());
        assertEquals(10000L, response.getCategories().get(0).getDisplayOrder());
    }

    @Test
    void getWordDetailShouldIncludeCategoryName() {
        Category category = new Category();
        category.setId(1L);
        category.setName("TOTOTP");

        Word word = new Word();
        word.setId(1L);
        word.setWord("serendipity");
        word.setMeaning("A pleasant surprise");
        word.setCategories(new LinkedHashSet<>(Set.of(category)));
        word.setQuizModes(Set.of(QuizMode.IMAGE, QuizMode.TEXT));
        word.setCategoryDisplayOrder(1L, 10000L);
        word.setCreatedAt(LocalDateTime.now());
        word.setUpdatedAt(LocalDateTime.now());

        WordRepo wordRepo = (WordRepo) Proxy.newProxyInstance(
                WordRepo.class.getClassLoader(),
                new Class<?>[]{WordRepo.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        return Optional.of(word);
                    }
                    if (method.getReturnType().equals(Optional.class)) {
                        return Optional.empty();
                    }
                    if (method.getReturnType().equals(boolean.class)) {
                        return false;
                    }
                    if (method.getReturnType().equals(int.class)) {
                        return 0;
                    }
                    return null;
                }
        );

        CategoryRepo categoryRepo = (CategoryRepo) Proxy.newProxyInstance(
                CategoryRepo.class.getClassLoader(),
                new Class<?>[]{CategoryRepo.class},
                (proxy, method, args) -> {
                    if (method.getReturnType().equals(Optional.class)) {
                        return Optional.empty();
                    }
                    if (method.getReturnType().equals(boolean.class)) {
                        return false;
                    }
                    return null;
                }
        );

        WordServiceImpl wordService = new WordServiceImpl(
                wordRepo,
                categoryRepo,
                new ModelMapper(),
                mock(ImageStorageService.class),
                new ObjectMapper()
        );

        WordDetailResponseDTO response = wordService.getWordDetail(1L);

        assertNotNull(response);
        assertEquals("TOTOTP", response.getCategories().get(0).getCategoryName());
        assertEquals(1L, response.getCategories().get(0).getCategoryId());
    }

    @Test
    void getWordsByCategoryShouldAcceptNumericCategoryId() {
        Category category = new Category();
        category.setId(1L);
        category.setName("TOTOTP");

        Word word = new Word();
        word.setId(1L);
        word.setWord("serendipity");
        word.setMeaning("A pleasant surprise");
        word.setCategories(new LinkedHashSet<>(Set.of(category)));
        word.setCategoryDisplayOrder(1L, 10000L);

        WordRepo wordRepo = (WordRepo) Proxy.newProxyInstance(
                WordRepo.class.getClassLoader(),
                new Class<?>[]{WordRepo.class},
                (proxy, method, args) -> {
                    if ("findByCategoriesContaining".equals(method.getName())) {
                        return new PageImpl<>(List.of(word), PageRequest.of(0, 10), 1);
                    }
                    if (method.getReturnType().equals(Optional.class)) {
                        return Optional.empty();
                    }
                    if (method.getReturnType().equals(boolean.class)) {
                        return false;
                    }
                    if (method.getReturnType().equals(int.class)) {
                        return 0;
                    }
                    return null;
                }
        );

        CategoryRepo categoryRepo = (CategoryRepo) Proxy.newProxyInstance(
                CategoryRepo.class.getClassLoader(),
                new Class<?>[]{CategoryRepo.class},
                (proxy, method, args) -> {
                    if ("findById".equals(method.getName())) {
                        return Optional.of(category);
                    }
                    if ("findByName".equals(method.getName())) {
                        return Optional.empty();
                    }
                    if (method.getReturnType().equals(Optional.class)) {
                        return Optional.empty();
                    }
                    if (method.getReturnType().equals(boolean.class)) {
                        return false;
                    }
                    if (method.getReturnType().equals(int.class)) {
                        return 0;
                    }
                    return null;
                }
        );

        WordServiceImpl wordService = new WordServiceImpl(
                wordRepo,
                categoryRepo,
                new ModelMapper(),
                mock(ImageStorageService.class),
                new ObjectMapper()
        );

        Page<WordResponseDTO> response = wordService.getWordsByCategory("1", PageRequest.of(0, 10));

        assertNotNull(response);
        assertEquals(1L, response.getTotalElements());
        assertEquals("TOTOTP", response.getContent().get(0).getCategories().get(0).getCategoryName());
    }
}
