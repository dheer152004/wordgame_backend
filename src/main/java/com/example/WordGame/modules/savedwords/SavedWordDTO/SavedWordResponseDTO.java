package com.example.WordGame.modules.savedwords.SavedWordDTO;

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
    private java.util.List<String> images;
    private String categoryName;
    private String notes;
}
