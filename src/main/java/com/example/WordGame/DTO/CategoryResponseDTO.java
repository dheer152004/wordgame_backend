package com.example.WordGame.DTO;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private Long wordCount;  // How many words in this category
}

