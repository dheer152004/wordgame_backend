package com.example.WordGame.DTO.Word;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class WordRequestDTO {
    private String word;
    private String meaning;
    private Long categoryId;
    private MultipartFile memeImage;
    private List<String> examples;
}
