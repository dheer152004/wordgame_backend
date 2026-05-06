package com.example.WordGame.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WordDetailResponseDTO {
    private Long id;
    private String word;
    private String meaning;
    private String memeImageUrl;
    private String categoryName;
    private List<String> examples;
}
