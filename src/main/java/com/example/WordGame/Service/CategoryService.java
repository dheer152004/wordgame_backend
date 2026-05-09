package com.example.WordGame.Service;

import com.example.WordGame.DTO.Word.CategoryResponseDTO;

import java.util.List;

public interface CategoryService {
    public List<CategoryResponseDTO> getAllCategories();
}
