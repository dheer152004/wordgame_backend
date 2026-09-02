package com.example.WordGame.modules.words.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.WordGame.modules.words.DTO.SourceCreditsDTO;
import com.example.WordGame.modules.words.PartOfSpeech;
import com.example.WordGame.modules.words.WordType;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WordResponseDTO {

    private Long id;
    private String word;
    private WordType wordType;
    private String expandedForm;
    private PartOfSpeech partOfSpeech;
    private Long categoryId;
    private String categoryName;
    private String meaning;
    private Long displayOrder;
    private List<String> images;
    private String description;
    private List<String> facts;
    private List<String> examples;
    private java.util.List<String> quizModes;
    private List<RelatedWord> relatedWordIds = new java.util.ArrayList<>();
    private List<AlsoAppearsIn> alsoAppearsIn = new java.util.ArrayList<>();
    @JsonProperty("source&credits")
    private SourceCreditsDTO sourceAndCredits;
    private String created;
    private String updated;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RelatedWord {
        private Long wordId;
        private String word;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AlsoAppearsIn {
        private Long categoryId;
        private Long wordId;
        private String categoryName;
        private String word;
    }
}
