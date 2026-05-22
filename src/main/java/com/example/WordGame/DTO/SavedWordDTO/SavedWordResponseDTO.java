package com.example.WordGame.DTO.SavedWordDTO;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
public class SavedWordResponseDTO {
    private Long savedWordId;
    private Long wordId;
    private String word;
    private String meaning;
    private String memeImageUrl;
    private String categoryName;
    private String notes;
    private LocalDateTime savedAt;
}
