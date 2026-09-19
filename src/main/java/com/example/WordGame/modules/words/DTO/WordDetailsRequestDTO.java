package com.example.WordGame.modules.words.DTO;
import lombok.Data;

@Data
public class WordDetailsRequestDTO {

    private Long id;

    private String expandedForm;

    private String usage;

    private String etymology;
}
