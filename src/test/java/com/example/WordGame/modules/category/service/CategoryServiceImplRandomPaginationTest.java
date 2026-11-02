package com.example.WordGame.modules.category.service;

import com.example.WordGame.Service.ImageStorageService;
import com.example.WordGame.modules.category.CategoryDTO.CategoryResponseDTO;
import com.example.WordGame.modules.category.Entities.Category;
import com.example.WordGame.modules.category.repository.CategoryRepo;
import com.example.WordGame.modules.genre.repository.GenreRepo;
import com.example.WordGame.modules.words.repository.WordRepo;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentMatchers;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.LongStream;
import java.util.stream.StreamSupport;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CategoryServiceImplRandomPaginationTest {

    @Test
    void randomPagesShareOneOrderWithoutRepeatingCategoriesAndNewSessionGetsNewOrder() {
        CategoryRepo categoryRepo = mock(CategoryRepo.class);
        WordRepo wordRepo = mock(WordRepo.class);
        when(categoryRepo.findAllCategoryIds())
                .thenReturn(LongStream.rangeClosed(1, 25).boxed().toList());
        when(categoryRepo.findAllById(ArgumentMatchers.<Iterable<Long>>any())).thenAnswer(invocation -> {
            Iterable<Long> ids = invocation.getArgument(0);
            List<Category> categories = new ArrayList<>();
            for (Long id : ids) {
                Category category = new Category();
                category.setId(id);
                categories.add(category);
            }
            return categories;
        });
        when(wordRepo.countByCategoriesContaining(any(Category.class))).thenReturn(0L);

        CategoryServiceImpl service = new CategoryServiceImpl(
                categoryRepo,
                mock(GenreRepo.class),
                wordRepo,
                new ModelMapper(),
                mock(ImageStorageService.class),
                new ConcurrentMapCacheManager("randomCategories"));

        String firstSessionToken = UUID.randomUUID().toString();
        Page<CategoryResponseDTO> firstPage =
                service.getRandomCategories(firstSessionToken, PageRequest.of(0, 10));
        Page<CategoryResponseDTO> secondPage =
                service.getRandomCategories(firstSessionToken, PageRequest.of(1, 10));
        Page<CategoryResponseDTO> thirdPage =
                service.getRandomCategories(firstSessionToken, PageRequest.of(2, 10));

        List<Long> firstSequence = new ArrayList<>();
        firstSequence.addAll(ids(firstPage));
        firstSequence.addAll(ids(secondPage));
        firstSequence.addAll(ids(thirdPage));
        assertEquals(25, firstSequence.size());
        assertEquals(25, firstSequence.stream().distinct().count());
        assertTrue(firstPage.hasNext());
        assertTrue(secondPage.hasNext());
        assertFalse(thirdPage.hasNext());

        String secondSessionToken = UUID.randomUUID().toString();
        Page<CategoryResponseDTO> newSessionFirstPage =
                service.getRandomCategories(secondSessionToken, PageRequest.of(0, 10));
        assertNotEquals(ids(firstPage), ids(newSessionFirstPage));
    }

    private List<Long> ids(Page<CategoryResponseDTO> page) {
        return page.getContent().stream().map(CategoryResponseDTO::getId).toList();
    }
}
