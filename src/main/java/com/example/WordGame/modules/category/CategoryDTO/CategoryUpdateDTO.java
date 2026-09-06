package com.example.WordGame.modules.category.CategoryDTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import com.example.WordGame.modules.category.enums.AgeRating;

@Data
public class CategoryUpdateDTO {
    private Long genreId;
    private String name;
    private String description;
    private String imageUrl;
    private MultipartFile image;
    private Boolean isActive;
    private AgeRating ageRating;
}
