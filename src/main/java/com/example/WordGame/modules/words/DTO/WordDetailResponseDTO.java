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
    private WordLanguageResponseDTO language;
    private List<WordGrammarValueResponseDTO> grammarValues;
    private WordDetailsRequestDTO wordDetails;
    private List<WordCategoryDTO> categories;
    private Long redirectCategoryId;
    private String redirectCategoryName;
    private RedirectWebsiteDTO redirectWebsite;
    private String meaning;
    private String description;
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
