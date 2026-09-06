package com.example.WordGame.modules.words.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.example.WordGame.modules.words.PartOfSpeech;
import com.example.WordGame.modules.words.WordType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class WordDetailResponseDTO {
    private Long id;
    private String word;
    private WordType wordType;
    private String expandedForm;
    private PartOfSpeech partOfSpeech;
    private Long categoryId;
    private String categoryName;
    private String meaning;
    private Long displayOrder;
    private String description;
    @com.fasterxml.jackson.annotation.JsonProperty("source&credits")
    private com.example.WordGame.modules.words.DTO.SourceCreditsDTO sourceAndCredits;
    private List<ImageUrlDTO> images;
    private List<VideoUrlDTO> videos;
    private List<AudioUrlDTO> audios;
    private List<String> facts;
    private List<String> examples;
    private java.util.List<String> quizModes;
    private List<WordResponseDTO.RelatedWord> relatedWordIds = new java.util.ArrayList<>();
    private List<WordResponseDTO.AlsoAppearsIn> alsoAppearsIn = new java.util.ArrayList<>();
    private String created;
    private String updated;
}
