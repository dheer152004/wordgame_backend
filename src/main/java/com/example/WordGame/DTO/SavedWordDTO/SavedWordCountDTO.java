package com.example.WordGame.DTO.SavedWordDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SavedWordCountDTO {
    private Long totalSavedWords;
}
