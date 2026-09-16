package com.example.WordGame.modules.language.service;

import com.example.WordGame.exceptions.ApiException;
import com.example.WordGame.modules.language.DTO.LanguageRequestDTO;
import com.example.WordGame.modules.language.DTO.LanguageResponseDTO;
import com.example.WordGame.modules.language.DTO.LanguageUpdateDTO;
import com.example.WordGame.modules.language.entity.Language;
import com.example.WordGame.modules.language.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LanguageServiceImpl implements LanguageService {

    private final LanguageRepository languageRepository;

    @Override
    public List<LanguageResponseDTO> getAllLanguages() {
        return languageRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public LanguageResponseDTO getLanguageById(Long id) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ApiException("Language not found with id: " + id));
        return toResponseDTO(language);
    }

    @Override
    @Transactional
    public LanguageResponseDTO createLanguage(LanguageRequestDTO request) {
        Language language = new Language();
        language.setCode(request.getCode());
        language.setName(request.getName());
        language.setGrammarName(request.getGrammarName());
        language.setGrammarDescription(request.getGrammarDescription());
        language.setGrammarActive(request.getGrammarActive() != null ? request.getGrammarActive() : false);
        language.setDisplayOrder(request.getDisplayOrder());
        language.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return toResponseDTO(languageRepository.save(language));
    }

    @Override
    @Transactional
    public LanguageResponseDTO updateLanguage(Long id, LanguageUpdateDTO request) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ApiException("Language not found with id: " + id));

        if (request.getCode() != null) language.setCode(request.getCode());
        if (request.getName() != null) language.setName(request.getName());
        if (request.getGrammarName() != null) language.setGrammarName(request.getGrammarName());
        if (request.getGrammarDescription() != null) language.setGrammarDescription(request.getGrammarDescription());
        if (request.getGrammarActive() != null) language.setGrammarActive(request.getGrammarActive());
        if (request.getDisplayOrder() != null) language.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) language.setIsActive(request.getIsActive());

        return toResponseDTO(languageRepository.save(language));
    }

    @Override
    @Transactional
    public LanguageResponseDTO patchLanguage(Long id, LanguageUpdateDTO request) {
        return updateLanguage(id, request);
    }

    @Override
    @Transactional
    public void deleteLanguage(Long id) {
        Language language = languageRepository.findById(id)
                .orElseThrow(() -> new ApiException("Language not found with id: " + id));
        languageRepository.delete(language);
    }

    private LanguageResponseDTO toResponseDTO(Language language) {
        LanguageResponseDTO response = new LanguageResponseDTO();
        response.setId(language.getId());
        response.setCode(language.getCode());
        response.setName(language.getName());
        response.setGrammarName(language.getGrammarName());
        response.setGrammarDescription(language.getGrammarDescription());
        response.setGrammarActive(language.getGrammarActive());
        response.setDisplayOrder(language.getDisplayOrder());
        response.setIsActive(language.getIsActive());
        return response;
    }
}
