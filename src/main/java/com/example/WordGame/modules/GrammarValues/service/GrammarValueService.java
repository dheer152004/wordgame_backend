package com.example.WordGame.modules.GrammarValues.service;

import com.example.WordGame.modules.GrammarValues.DTO.GrammarValueRequestDTO;
import com.example.WordGame.modules.GrammarValues.DTO.GrammarValueResponseDTO;
import com.example.WordGame.modules.GrammarValues.DTO.GrammarValueUpdateDTO;

import java.util.List;

public interface GrammarValueService {
    List<GrammarValueResponseDTO> getAllGrammarValues();
    GrammarValueResponseDTO getGrammarValueById(Long id);
    GrammarValueResponseDTO createGrammarValue(GrammarValueRequestDTO request);
    GrammarValueResponseDTO updateGrammarValue(Long id, GrammarValueUpdateDTO request);
    GrammarValueResponseDTO patchGrammarValue(Long id, GrammarValueUpdateDTO request);
    void deleteGrammarValue(Long id);
}
