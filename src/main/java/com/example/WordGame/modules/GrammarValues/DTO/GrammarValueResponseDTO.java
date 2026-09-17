package com.example.WordGame.modules.GrammarValues.DTO;

import lombok.Data;

@Data
public class GrammarValueResponseDTO {
    private Long id;
    private Long grammarCategoryId;
    private String grammarCategoryName;
    private Long languageId;
    private String languageCode;
    private String languageName;
    private String name;
    private String displayName;
    private String description;
    private Long displayOrder;
    private Boolean isActive;
}
