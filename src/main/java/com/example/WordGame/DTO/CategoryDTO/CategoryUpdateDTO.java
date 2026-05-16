package com.example.WordGame.DTO.CategoryDTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class CategoryUpdateDTO {
    private String name;
    private String description;
    private MultipartFile image;
    private Boolean isActive;
}
