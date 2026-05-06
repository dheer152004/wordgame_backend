package com.example.WordGame.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WordResponseDTO {

    private Long id;
    private String word;
    private String meaning;
    private String memeImageUrl;
    private String categoryName;
}
