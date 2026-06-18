package com.example.WordGame.modules.words.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WordDetailResponseDTO {
    private Long id;
    private String word;
    private Long categoryId;
    private String meaning;
    private String description;
    @com.fasterxml.jackson.annotation.JsonProperty("source&credits")
    private com.example.WordGame.modules.words.DTO.SourceCreditsDTO sourceAndCredits;
    private List<String> images;
    private List<String> facts;
    private List<String> examples;
    private List<WordResponseDTO.RelatedWord> relatedWordIds;
    private List<WordResponseDTO.AlsoAppearsIn> alsoAppearsIn;
    private String created;
    private String updated;
}
