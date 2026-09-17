package com.example.WordGame.modules.GrammarCategories.DTO;

import lombok.Data;

@Data
public class GrammarCategoryResponseDTO {
    private Long id;
    private Long languageId;
    private String languageName;
    private String name;
    private String displayName;
    private String description;
    private Long displayOrder;
    private Boolean isActive;
}
