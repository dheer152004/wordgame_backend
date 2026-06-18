package com.example.WordGame.modules.savedwords.SavedWordDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SavedWordCountDTO {
    private Long totalSavedWords;
}
