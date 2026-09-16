package com.example.WordGame.modules.language.service;

import com.example.WordGame.modules.language.DTO.LanguageRequestDTO;
import com.example.WordGame.modules.language.DTO.LanguageResponseDTO;
import com.example.WordGame.modules.language.DTO.LanguageUpdateDTO;

import java.util.List;

public interface LanguageService {
    List<LanguageResponseDTO> getAllLanguages();
    LanguageResponseDTO getLanguageById(Long id);
    LanguageResponseDTO createLanguage(LanguageRequestDTO request);
    LanguageResponseDTO updateLanguage(Long id, LanguageUpdateDTO request);
    LanguageResponseDTO patchLanguage(Long id, LanguageUpdateDTO request);
    void deleteLanguage(Long id);
}
