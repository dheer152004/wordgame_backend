package com.example.WordGame.modules.savedwords.SavedWordDTO;

import lombok.Data;
import jakarta.validation.constraints.NotNull;

@Data
public class SaveWordRequestDTO {
    @NotNull(message = "Word ID is required")
    private Long wordId;

    private String notes;
}