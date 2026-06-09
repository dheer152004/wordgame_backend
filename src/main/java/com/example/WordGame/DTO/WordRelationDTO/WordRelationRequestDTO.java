package com.example.WordGame.DTO.WordRelationDTO;

import lombok.Data;

@Data
public class WordRelationRequestDTO {
    private Long wordId;
    private Long relatedWordId;
    private String relationType;
}