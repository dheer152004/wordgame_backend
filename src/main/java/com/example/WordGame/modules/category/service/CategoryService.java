package com.example.WordGame.modules.category.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.WordGame.modules.category.CategoryDTO.CategoryRequestDTO;
import com.example.WordGame.modules.category.CategoryDTO.CategoryResponseDTO;
import com.example.WordGame.modules.category.CategoryDTO.CategoryUpdateDTO;

public interface CategoryService {
    public List<CategoryResponseDTO> getAllCategories();
    public Page<CategoryResponseDTO> searchCategories(String q, Pageable pageable);
    public CategoryResponseDTO toggleCategoryStatus(Long id);
    public void deleteCategory(Long id);
    public CategoryResponseDTO updateCategory(Long id, CategoryUpdateDTO request);
    public CategoryResponseDTO createCategory(CategoryRequestDTO request);
    public CategoryResponseDTO getCategoryById(Long id);
}
