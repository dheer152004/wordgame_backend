package com.example.WordGame.modules.genre.GenreDTO;

import lombok.Data;

@Data
public class GenreResponseDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private String description;
    private Long categoryCount;
}