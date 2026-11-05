package com.example.WordGame.modules.words.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class VideoUrlDTO {
    private String videoUrl;
    private MediaCreditsDTO credits;

    public VideoUrlDTO(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void setCredits(MediaCreditsDTO credits) {
        this.credits = credits == null || credits.isEmpty() ? null : credits;
    }
}