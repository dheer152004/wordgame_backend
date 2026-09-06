package com.example.WordGame.modules.words.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.example.WordGame.modules.words.PartOfSpeech;
import com.example.WordGame.modules.words.WordType;

@Data
public class WordRequestDTO {
    private String word;
    private WordType wordType;
    private String expandedForm;
    private PartOfSpeech partOfSpeech;
    private String meaning;
    private String description;
    private Long categoryId;
    private Long displayOrder;
    private List<MultipartFile> wordImages;
    private List<MultipartFile> wordVideos;
    private List<MultipartFile> wordAudios;
    private List<String> imageUrls;
    private List<ImageUrlDTO> images;
    private List<VideoUrlDTO> videos;
    private List<AudioUrlDTO> audios;
    
    private List<String> facts;
    private List<String> examples;
    private List<Long> relatedWordIds;
    private java.util.List<String> quizModes;
    private List<Map<String, Long>> alsoAppearsIn;
    @JsonProperty("source&credits")
    private SourceCreditsDTO sourceAndCredits;
}
