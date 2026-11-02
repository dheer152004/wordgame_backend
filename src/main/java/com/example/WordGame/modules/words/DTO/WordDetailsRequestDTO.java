package com.example.WordGame.modules.words.DTO;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WordDetailsRequestDTO {

    private Long id;

    private String expandedForm;

    private String usage;

    private String etymology;
}
