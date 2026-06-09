package com.example.WordGame.DTO.CategoryDTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CategoryUpdateDTO {
    private Long genreId;
    private String name;
    private String description;
    private String imageUrl;
    private MultipartFile image;
    private Boolean isActive;
}
