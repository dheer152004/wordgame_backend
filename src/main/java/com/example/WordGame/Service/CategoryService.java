package com.example.WordGame.Service;

import com.example.WordGame.DTO.CategoryDTO.CategoryRequestDTO;
import com.example.WordGame.DTO.CategoryDTO.CategoryResponseDTO;
import com.example.WordGame.DTO.CategoryDTO.CategoryUpdateDTO;

import java.util.List;

public interface CategoryService {
    public List<CategoryResponseDTO> getAllCategories();
    public CategoryResponseDTO toggleCategoryStatus(Long id);
    public void deleteCategory(Long id);
    public CategoryResponseDTO updateCategory(Long id, CategoryUpdateDTO request);
    public CategoryResponseDTO createCategory(CategoryRequestDTO request);
    public CategoryResponseDTO getCategoryById(Long id);
}
