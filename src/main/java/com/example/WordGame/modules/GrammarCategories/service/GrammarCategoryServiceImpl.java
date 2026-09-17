package com.example.WordGame.modules.GrammarCategories.service;

import com.example.WordGame.exceptions.ApiException;
import com.example.WordGame.modules.GrammarCategories.DTO.GrammarCategoryRequestDTO;
import com.example.WordGame.modules.GrammarCategories.DTO.GrammarCategoryResponseDTO;
import com.example.WordGame.modules.GrammarCategories.DTO.GrammarCategoryUpdateDTO;
import com.example.WordGame.modules.GrammarCategories.entity.GrammarCategory;
import com.example.WordGame.modules.GrammarCategories.repository.GrammarCategoryRepository;
import com.example.WordGame.modules.language.entity.Language;
import com.example.WordGame.modules.language.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrammarCategoryServiceImpl implements GrammarCategoryService {

    private final GrammarCategoryRepository grammarCategoryRepository;
    private final LanguageRepository languageRepository;

    @Override
    public List<GrammarCategoryResponseDTO> getAllGrammarCategories() {
        return grammarCategoryRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GrammarCategoryResponseDTO getGrammarCategoryById(Long id) {
        GrammarCategory grammarCategory = grammarCategoryRepository.findById(id)
                .orElseThrow(() -> new ApiException("Grammar category not found with id: " + id));
        return toResponseDTO(grammarCategory);
    }

    @Override
    @Transactional
    public GrammarCategoryResponseDTO createGrammarCategory(GrammarCategoryRequestDTO request) {
        if (request.getLanguageId() == null) {
            throw new ApiException("languageId is required");
        }

        Language language = languageRepository.findById(request.getLanguageId())
                .orElseThrow(() -> new ApiException("Language not found with id: " + request.getLanguageId()));

        GrammarCategory grammarCategory = new GrammarCategory();
        grammarCategory.setLanguage(language);
        grammarCategory.setName(request.getName());
        grammarCategory.setDisplayName(request.getDisplayName());
        grammarCategory.setDescription(request.getDescription());
        grammarCategory.setDisplayOrder(request.getDisplayOrder());
        grammarCategory.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return toResponseDTO(grammarCategoryRepository.save(grammarCategory));
    }

    @Override
    @Transactional
    public GrammarCategoryResponseDTO updateGrammarCategory(Long id, GrammarCategoryUpdateDTO request) {
        GrammarCategory grammarCategory = grammarCategoryRepository.findById(id)
                .orElseThrow(() -> new ApiException("Grammar category not found with id: " + id));

        if (request.getLanguageId() != null) {
            Language language = languageRepository.findById(request.getLanguageId())
                    .orElseThrow(() -> new ApiException("Language not found with id: " + request.getLanguageId()));
            grammarCategory.setLanguage(language);
        }

        if (request.getName() != null) grammarCategory.setName(request.getName());
        if (request.getDisplayName() != null) grammarCategory.setDisplayName(request.getDisplayName());
        if (request.getDescription() != null) grammarCategory.setDescription(request.getDescription());
        if (request.getDisplayOrder() != null) grammarCategory.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) grammarCategory.setIsActive(request.getIsActive());

        return toResponseDTO(grammarCategoryRepository.save(grammarCategory));
    }

    @Override
    @Transactional
    public GrammarCategoryResponseDTO patchGrammarCategory(Long id, GrammarCategoryUpdateDTO request) {
        return updateGrammarCategory(id, request);
    }

    @Override
    @Transactional
    public void deleteGrammarCategory(Long id) {
        GrammarCategory grammarCategory = grammarCategoryRepository.findById(id)
                .orElseThrow(() -> new ApiException("Grammar category not found with id: " + id));
        grammarCategoryRepository.delete(grammarCategory);
    }

    private GrammarCategoryResponseDTO toResponseDTO(GrammarCategory grammarCategory) {
        GrammarCategoryResponseDTO response = new GrammarCategoryResponseDTO();
        response.setId(grammarCategory.getId());
        response.setLanguageId(grammarCategory.getLanguage() != null ? grammarCategory.getLanguage().getId() : null);
        response.setLanguageName(grammarCategory.getLanguage() != null ? grammarCategory.getLanguage().getName() : null);
        response.setName(grammarCategory.getName());
        response.setDisplayName(grammarCategory.getDisplayName());
        response.setDescription(grammarCategory.getDescription());
        response.setDisplayOrder(grammarCategory.getDisplayOrder());
        response.setIsActive(grammarCategory.getIsActive());
        return response;
    }
}
