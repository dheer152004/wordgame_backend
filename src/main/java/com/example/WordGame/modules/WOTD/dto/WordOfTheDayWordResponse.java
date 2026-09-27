package com.example.WordGame.modules.WOTD.dto;

import com.example.WordGame.modules.words.DTO.AudioUrlDTO;
import com.example.WordGame.modules.words.DTO.ImageUrlDTO;
import com.example.WordGame.modules.words.DTO.VideoUrlDTO;
import com.example.WordGame.modules.words.DTO.WordCategoryDTO;
import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class WordOfTheDayWordResponse {
    Long wordId;
    String word;
    String wordType;
    String partOfSpeech;
    String meaning;
    String description;
    List<ImageUrlDTO> images;
    List<VideoUrlDTO> videos;
    List<AudioUrlDTO> audios;
    List<String> facts;
    List<String> examples;
    List<WordCategoryDTO> categories;
}