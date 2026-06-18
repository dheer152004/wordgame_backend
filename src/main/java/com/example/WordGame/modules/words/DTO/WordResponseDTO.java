package com.example.WordGame.modules.words.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.WordGame.modules.words.DTO.SourceCreditsDTO;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WordResponseDTO {

    private Long id;
    private String word;
    private Long categoryId;
    private String meaning;
    private List<String> images;
    private String description;
    private List<String> facts;
    private List<String> examples;
    private List<RelatedWord> relatedWordIds;
    private List<AlsoAppearsIn> alsoAppearsIn;
    @JsonProperty("source&credits")
    private SourceCreditsDTO sourceAndCredits;
    private String created;
    private String updated;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class RelatedWord {
        private Long id;
        private String word;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class AlsoAppearsIn {
        private Long categoryId;
        private Long wordId;
    }
}
