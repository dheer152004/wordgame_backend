package com.example.WordGame.modules.words.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordCategoryDTO {
    private Long categoryId;
    private String categoryName;
    private Long displayOrder;
}