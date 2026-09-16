package com.example.WordGame.modules.language.DTO;

import lombok.Data;

@Data
public class LanguageRequestDTO {
    private String code;
    private String name;
    private String grammarName;
    private String grammarDescription;
    private Boolean grammarActive;
    private Long displayOrder;
    private Boolean isActive;
}
