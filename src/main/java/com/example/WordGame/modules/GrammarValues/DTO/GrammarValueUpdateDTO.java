package com.example.WordGame.modules.GrammarValues.DTO;

import lombok.Data;

@Data
public class GrammarValueUpdateDTO {
    private Long grammarCategoryId;
    private String name;
    private String displayName;
    private String description;
    private Long displayOrder;
    private Boolean isActive;
}
