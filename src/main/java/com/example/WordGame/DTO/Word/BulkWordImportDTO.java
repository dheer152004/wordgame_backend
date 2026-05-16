package com.example.WordGame.DTO.Word;

import lombok.Data;
import java.util.List;

@Data
public class BulkWordImportDTO {
    private List<WordEntry> words;

    @Data
    public static class WordEntry {
        private String word;
        private String meaning;
        private String memeImageUrl;  // Can be existing URL
        private List<String> examples;
    }
}