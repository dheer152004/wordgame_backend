package com.example.WordGame.modules.category.controller;

import com.example.WordGame.modules.category.CategoryDTO.CategoryResponseDTO;
import com.example.WordGame.modules.category.service.CategoryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    @Test
    void getAllCategoriesUsesTenItemPagesAndReturnsPaginationMetadata() {
        CategoryResponseDTO category = new CategoryResponseDTO();
        Pageable requestedPage = Pageable.ofSize(10).withPage(1);
        Page<CategoryResponseDTO> categoriesPage =
                new PageImpl<>(List.of(category), requestedPage, 21);
        when(categoryService.getAllCategories(any(Pageable.class))).thenReturn(categoriesPage);

        ResponseEntity<Map<String, Object>> response = categoryController.getAllCategories(1);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(List.of(category), body.get("categories"));
        assertEquals(1, body.get("currentPage"));
        assertEquals(3, body.get("totalPages"));
        assertEquals(21L, body.get("totalCategories"));
        assertTrue((Boolean) body.get("hasMore"));
        assertEquals(10, body.get("pageSize"));

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(categoryService).getAllCategories(pageableCaptor.capture());
        Pageable pageable = pageableCaptor.getValue();
        assertEquals(1, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
        assertEquals(Sort.by("id").ascending(), pageable.getSort());
    }

    @Test
    void randomCategoriesStartsANewSessionAndReturnsItsToken() {
        CategoryResponseDTO category = new CategoryResponseDTO();
        Pageable requestedPage = Pageable.ofSize(10);
        Page<CategoryResponseDTO> categoriesPage =
                new PageImpl<>(List.of(category), requestedPage, 21);
        when(categoryService.getRandomCategories(any(String.class), any(Pageable.class)))
                .thenReturn(categoriesPage);

        ResponseEntity<Map<String, Object>> response =
                categoryController.getRandomCategories(0, null);

        Map<String, Object> body = response.getBody();
        assertNotNull(body);
        assertEquals(List.of(category), body.get("categories"));
        assertEquals(0, body.get("currentPage"));
        assertEquals(3, body.get("totalPages"));
        assertEquals(21L, body.get("totalCategories"));
        assertEquals(10, body.get("pageSize"));
        assertNotNull(body.get("paginationToken"));

        ArgumentCaptor<String> tokenCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(categoryService).getRandomCategories(tokenCaptor.capture(), pageableCaptor.capture());
        assertEquals(10, pageableCaptor.getValue().getPageSize());
        assertEquals(0, pageableCaptor.getValue().getPageNumber());
        assertEquals(body.get("paginationToken"), tokenCaptor.getValue());
    }

    @Test
    void randomCategoriesRequiresTokenOnSubsequentPages() {
        ResponseStatusException exception = org.junit.jupiter.api.Assertions.assertThrows(
                ResponseStatusException.class,
                () -> categoryController.getRandomCategories(1, null));

        assertEquals(400, exception.getStatusCode().value());
    }
}
