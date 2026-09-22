package com.example.WordGame.modules.words.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordGrammarValueResponseDTO {
    private String grammarCategory;
    private String grammarValue;
}
