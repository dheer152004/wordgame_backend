package com.example.WordGame.modules.category.CategoryDTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CategoryRequestDTO {
    private Long genreId;
    private String name;
    private String description;
    private String imageUrl;
    private MultipartFile image;
}