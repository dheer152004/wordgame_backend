package com.example.WordGame.DTO.WordRelationDTO;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class WordRelationResponseDTO {
    private Long id;
    private Long wordId;
    private String word;
    private Long relatedWordId;
    private String relatedWord;
    private String relationType;
    private LocalDateTime createdAt;
}