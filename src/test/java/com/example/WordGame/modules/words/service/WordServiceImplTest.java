package com.example.WordGame.modules.words.service;

import com.example.WordGame.Service.AzureImageUploadService;
import com.example.WordGame.modules.category.Entities.Category;
import com.example.WordGame.modules.category.repository.CategoryRepo;
import com.example.WordGame.modules.words.DTO.WordRequestDTO;
import com.example.WordGame.modules.words.DTO.WordResponseDTO;
import com.example.WordGame.modules.words.Entities.Word;
import com.example.WordGame.modules.words.QuizMode;
import com.example.WordGame.modules.words.repository.WordRepo;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import java.lang.reflect.Proxy;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class WordServiceImplTest {

    @Test
    void createWordShouldPopulateQuizModesInResponse() {
        Category category = new Category();
        category.setId(1L);
        category.setName("TOTOTP");

        WordRequestDTO request = new WordRequestDTO();
        request.setWord("serendipity");
        request.setMeaning("A pleasant surprise");
        request.setCategoryId(1L);
        request.setQuizModes(List.of("IMAGE", "TEXT"));

        Word savedWord = new Word();
        savedWord.setId(1L);
        savedWord.setWord("serendipity");
        savedWord.setMeaning("A pleasant surprise");
        savedWord.setCategory(category);
        savedWord.setQuizModes(Set.of(QuizMode.IMAGE, QuizMode.TEXT));

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
                new AzureImageUploadService(),
                new ObjectMapper()
        );

        WordResponseDTO response = wordService.createWord(request);

        assertNotNull(response);
        assertEquals(List.of("IMAGE", "TEXT"), response.getQuizModes());
    }
}
