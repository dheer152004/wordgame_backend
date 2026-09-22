package com.example.WordGame.modules.words.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordLanguageResponseDTO {
    private Long id;
    private String code;
    private String name;
}
