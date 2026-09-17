package com.example.WordGame.modules.GrammarCategories.service;

import com.example.WordGame.modules.GrammarCategories.DTO.GrammarCategoryRequestDTO;
import com.example.WordGame.modules.GrammarCategories.DTO.GrammarCategoryResponseDTO;
import com.example.WordGame.modules.GrammarCategories.DTO.GrammarCategoryUpdateDTO;

import java.util.List;

public interface GrammarCategoryService {
    List<GrammarCategoryResponseDTO> getAllGrammarCategories();
    GrammarCategoryResponseDTO getGrammarCategoryById(Long id);
    GrammarCategoryResponseDTO createGrammarCategory(GrammarCategoryRequestDTO request);
    GrammarCategoryResponseDTO updateGrammarCategory(Long id, GrammarCategoryUpdateDTO request);
    GrammarCategoryResponseDTO patchGrammarCategory(Long id, GrammarCategoryUpdateDTO request);
    void deleteGrammarCategory(Long id);
}
