package com.example.WordGame.modules.category.controller;

import com.example.WordGame.modules.category.CategoryDTO.CategoryResponseDTO;
import com.example.WordGame.modules.category.service.CategoryService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/categories", produces = "application/json")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CategoryController {

    private final CategoryService categoryService;

    // GET /api/categories - Get all categories for user to choose
    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAllCategories() {
        List<CategoryResponseDTO> categories = categoryService.getAllCategories();
        return ResponseEntity.ok(categories);
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