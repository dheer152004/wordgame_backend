package com.example.WordGame.Service.Impl;

import com.example.WordGame.DTO.WordRelationDTO.WordRelationRequestDTO;
import com.example.WordGame.DTO.WordRelationDTO.WordRelationResponseDTO;
import com.example.WordGame.Entities.Word;
import com.example.WordGame.Entities.WordRelation;
import com.example.WordGame.Repository.WordRelationRepo;
import com.example.WordGame.Repository.WordRepo;
import com.example.WordGame.Service.WordRelationService;
import com.example.WordGame.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WordRelationServiceImpl implements WordRelationService {

    private final WordRelationRepo wordRelationRepo;
    private final WordRepo wordRepo;

    @Override
    public List<WordRelationResponseDTO> getAllRelations() {
        return wordRelationRepo.findAll().stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    @Override
    public WordRelationResponseDTO getRelationById(Long id) {
        WordRelation relation = wordRelationRepo.findById(id)
                .orElseThrow(() -> new ApiException("Word relation not found with id: " + id));
        return toResponseDto(relation);
    }

    @Override
    @Transactional
    public WordRelationResponseDTO createRelation(WordRelationRequestDTO request) {
        WordRelation relation = buildRelation(new WordRelation(), request);
        relation.setCreatedAt(LocalDateTime.now());
        return toResponseDto(wordRelationRepo.save(relation));
    }

    @Override
    @Transactional
    public WordRelationResponseDTO updateRelation(Long id, WordRelationRequestDTO request) {
        WordRelation relation = wordRelationRepo.findById(id)
                .orElseThrow(() -> new ApiException("Word relation not found with id: " + id));
        return toResponseDto(wordRelationRepo.save(buildRelation(relation, request)));
    }

    @Override
    @Transactional
    public WordRelationResponseDTO patchRelation(Long id, WordRelationRequestDTO request) {
        return updateRelation(id, request);
    }

    @Override
    @Transactional
    public void deleteRelation(Long id) {
        WordRelation relation = wordRelationRepo.findById(id)
                .orElseThrow(() -> new ApiException("Word relation not found with id: " + id));
        wordRelationRepo.delete(relation);
    }

    private WordRelation buildRelation(WordRelation relation, WordRelationRequestDTO request) {
        if (request.getWordId() != null) {
            relation.setWord(resolveWord(request.getWordId()));
        }

        if (request.getRelatedWordId() != null) {
            relation.setRelatedWord(resolveWord(request.getRelatedWordId()));
        }

        if (relation.getWord() != null && relation.getRelatedWord() != null
            && relation.getWord().getId() == relation.getRelatedWord().getId()) {
            throw new ApiException("wordId and relatedWordId must be different");
        }

        if (request.getRelationType() != null) {
            relation.setRelationType(request.getRelationType());
        }

        if (relation.getWord() == null || relation.getRelatedWord() == null || relation.getRelationType() == null) {
            throw new ApiException("wordId, relatedWordId and relationType are required");
        }

        return relation;
    }

    private Word resolveWord(Long id) {
        return wordRepo.findById(id)
                .orElseThrow(() -> new ApiException("Word not found with id: " + id));
    }

    private WordRelationResponseDTO toResponseDto(WordRelation relation) {
        WordRelationResponseDTO response = new WordRelationResponseDTO();
        response.setId(relation.getId());
        response.setWordId(relation.getWord().getId());
        response.setWord(relation.getWord().getWord());
        response.setRelatedWordId(relation.getRelatedWord().getId());
        response.setRelatedWord(relation.getRelatedWord().getWord());
        response.setRelationType(relation.getRelationType());
        response.setCreatedAt(relation.getCreatedAt());
        return response;
    }
}