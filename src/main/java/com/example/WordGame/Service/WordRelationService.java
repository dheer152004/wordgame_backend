package com.example.WordGame.Service;

import com.example.WordGame.DTO.WordRelationDTO.WordRelationRequestDTO;
import com.example.WordGame.DTO.WordRelationDTO.WordRelationResponseDTO;

import java.util.List;

public interface WordRelationService {
    List<WordRelationResponseDTO> getAllRelations();

    WordRelationResponseDTO getRelationById(Long id);

    WordRelationResponseDTO createRelation(WordRelationRequestDTO request);

    WordRelationResponseDTO updateRelation(Long id, WordRelationRequestDTO request);

    WordRelationResponseDTO patchRelation(Long id, WordRelationRequestDTO request);

    void deleteRelation(Long id);
}