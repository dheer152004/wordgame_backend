package com.example.WordGame.modules.GrammarValues.service;

import com.example.WordGame.exceptions.ApiException;
import com.example.WordGame.modules.GrammarCategories.entity.GrammarCategory;
import com.example.WordGame.modules.GrammarCategories.repository.GrammarCategoryRepository;
import com.example.WordGame.modules.GrammarValues.DTO.GrammarValueRequestDTO;
import com.example.WordGame.modules.GrammarValues.DTO.GrammarValueResponseDTO;
import com.example.WordGame.modules.GrammarValues.DTO.GrammarValueUpdateDTO;
import com.example.WordGame.modules.GrammarValues.entity.GrammarValue;
import com.example.WordGame.modules.GrammarValues.repository.GrammarValueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GrammarValueServiceImpl implements GrammarValueService {

    private final GrammarValueRepository grammarValueRepository;
    private final GrammarCategoryRepository grammarCategoryRepository;

    @Override
    public List<GrammarValueResponseDTO> getAllGrammarValues() {
        return grammarValueRepository.findAll().stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GrammarValueResponseDTO getGrammarValueById(Long id) {
        GrammarValue grammarValue = grammarValueRepository.findById(id)
                .orElseThrow(() -> new ApiException("Grammar value not found with id: " + id));
        return toResponseDTO(grammarValue);
    }

    @Override
    @Transactional
    public GrammarValueResponseDTO createGrammarValue(GrammarValueRequestDTO request) {
        if (request.getGrammarCategoryId() == null) {
            throw new ApiException("grammarCategoryId is required");
        }

        GrammarCategory grammarCategory = grammarCategoryRepository.findById(request.getGrammarCategoryId())
                .orElseThrow(() -> new ApiException("Grammar category not found with id: " + request.getGrammarCategoryId()));

        GrammarValue grammarValue = new GrammarValue();
        grammarValue.setGrammarCategory(grammarCategory);
        grammarValue.setName(request.getName());
        grammarValue.setDisplayName(request.getDisplayName());
        grammarValue.setDescription(request.getDescription());
        Long maxDisplayOrder = grammarValueRepository
            .findMaxDisplayOrderByGrammarCategoryId(grammarCategory.getId());
        grammarValue.setDisplayOrder(maxDisplayOrder + 1);
        grammarValue.setIsActive(request.getIsActive() != null ? request.getIsActive() : true);

        return toResponseDTO(grammarValueRepository.save(grammarValue));
    }

    @Override
    @Transactional
    public GrammarValueResponseDTO updateGrammarValue(Long id, GrammarValueUpdateDTO request) {
        GrammarValue grammarValue = grammarValueRepository.findById(id)
                .orElseThrow(() -> new ApiException("Grammar value not found with id: " + id));

        if (request.getGrammarCategoryId() != null) {
            GrammarCategory grammarCategory = grammarCategoryRepository.findById(request.getGrammarCategoryId())
                    .orElseThrow(() -> new ApiException("Grammar category not found with id: " + request.getGrammarCategoryId()));
            grammarValue.setGrammarCategory(grammarCategory);
        }

        if (request.getName() != null) grammarValue.setName(request.getName());
        if (request.getDisplayName() != null) grammarValue.setDisplayName(request.getDisplayName());
        if (request.getDescription() != null) grammarValue.setDescription(request.getDescription());
        if (request.getDisplayOrder() != null) grammarValue.setDisplayOrder(request.getDisplayOrder());
        if (request.getIsActive() != null) grammarValue.setIsActive(request.getIsActive());

        return toResponseDTO(grammarValueRepository.save(grammarValue));
    }

    @Override
    @Transactional
    public GrammarValueResponseDTO patchGrammarValue(Long id, GrammarValueUpdateDTO request) {
        return updateGrammarValue(id, request);
    }

    @Override
    @Transactional
    public void deleteGrammarValue(Long id) {
        GrammarValue grammarValue = grammarValueRepository.findById(id)
                .orElseThrow(() -> new ApiException("Grammar value not found with id: " + id));
        grammarValueRepository.delete(grammarValue);
    }

    private GrammarValueResponseDTO toResponseDTO(GrammarValue grammarValue) {
        GrammarValueResponseDTO response = new GrammarValueResponseDTO();
        response.setId(grammarValue.getId());
        response.setGrammarCategoryId(grammarValue.getGrammarCategory() != null ? grammarValue.getGrammarCategory().getId() : null);
        response.setGrammarCategoryName(grammarValue.getGrammarCategory() != null ? grammarValue.getGrammarCategory().getName() : null);

        if (grammarValue.getGrammarCategory() != null && grammarValue.getGrammarCategory().getLanguage() != null) {
            response.setLanguageId(grammarValue.getGrammarCategory().getLanguage().getId());
            response.setLanguageCode(grammarValue.getGrammarCategory().getLanguage().getCode());
            response.setLanguageName(grammarValue.getGrammarCategory().getLanguage().getName());
        }

        response.setName(grammarValue.getName());
        response.setDisplayName(grammarValue.getDisplayName());
        response.setDescription(grammarValue.getDescription());
        response.setDisplayOrder(grammarValue.getDisplayOrder());
        response.setIsActive(grammarValue.getIsActive());
        return response;
    }
}
