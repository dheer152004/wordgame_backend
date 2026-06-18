package com.example.WordGame.modules.words.DTO;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class SourceCreditsDTO {
    private String author;
    private String platform;
    private String url;
    private String type;
    private String source;
    private String licences;
}
