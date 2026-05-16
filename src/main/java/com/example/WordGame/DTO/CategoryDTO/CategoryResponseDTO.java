package com.example.WordGame.DTO.CategoryDTO;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String imageUrl;
    private String description;
    private Boolean isActive;
    private Long wordCount;
}
