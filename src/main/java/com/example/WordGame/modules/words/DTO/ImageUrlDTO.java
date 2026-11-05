package com.example.WordGame.modules.words.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ImageUrlDTO {
    private String imageUrl;
    private MediaCreditsDTO credits;

    public ImageUrlDTO(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCredits(MediaCreditsDTO credits) {
        this.credits = credits == null || credits.isEmpty() ? null : credits;
    }
}