package com.example.WordGame.modules.category.controller;

import com.example.WordGame.modules.category.CategoryDTO.CategoryResponseDTO;
import com.example.WordGame.modules.category.service.CategoryService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


@RestController
@RequestMapping(value = "/api/categories", produces = "application/json")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private static final int CATEGORIES_PAGE_SIZE = 10;

    private final CategoryService categoryService;

    // GET /api/categories?page=0 - Get categories for user to choose
    @GetMapping
    public ResponseEntity<Map<String, Object>> getAllCategories(
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, CATEGORIES_PAGE_SIZE, Sort.by("id").ascending());
        Page<CategoryResponseDTO> categoriesPage = categoryService.getAllCategories(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("categories", categoriesPage.getContent());
        response.put("currentPage", categoriesPage.getNumber());
        response.put("totalPages", categoriesPage.getTotalPages());
        response.put("totalCategories", categoriesPage.getTotalElements());
        response.put("hasMore", categoriesPage.hasNext());
        response.put("pageSize", categoriesPage.getSize());

        return ResponseEntity.ok(response);
    }

    // GET /api/categories/random?page=0 - Start a new randomized category sequence
    @GetMapping("/random")
    public ResponseEntity<Map<String, Object>> getRandomCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(required = false) String paginationToken) {

        if (page == 0) {
            paginationToken = UUID.randomUUID().toString();
        } else if (paginationToken == null || paginationToken.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "paginationToken is required for pages after page 0");
        }

        Pageable pageable = PageRequest.of(page, CATEGORIES_PAGE_SIZE);
        Page<CategoryResponseDTO> categoriesPage =
                categoryService.getRandomCategories(paginationToken, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("categories", categoriesPage.getContent());
        response.put("currentPage", categoriesPage.getNumber());
        response.put("totalPages", categoriesPage.getTotalPages());
        response.put("totalCategories", categoriesPage.getTotalElements());
        response.put("hasMore", categoriesPage.hasNext());
        response.put("pageSize", categoriesPage.getSize());
        response.put("paginationToken", paginationToken);

        return ResponseEntity.ok(response);
    }

    // GET /api/categories/search?q=ser&page=0&size=10
    @GetMapping("/search")
    public ResponseEntity<Map<String, Object>> searchCategories(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        Page<CategoryResponseDTO> categoriesPage = categoryService.searchCategories(q, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("categories", categoriesPage.getContent());
        response.put("currentPage", categoriesPage.getNumber());
        response.put("totalPages", categoriesPage.getTotalPages());
        response.put("totalCategories", categoriesPage.getTotalElements());
        response.put("hasMore", categoriesPage.hasNext());
        response.put("query", q);

        return ResponseEntity.ok(response);
    }
}