package com.example.WordGame.modules.words.DTO;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class MediaCreditsDTO {
    private String author;
    private String platform;
    private String creditUrl;
    private String license;
    private String licenseUrl;
    private Boolean attributionRequired;
    private String attributionText;

    @JsonIgnore
    public boolean isEmpty() {
        return isBlank(author)
                && isBlank(platform)
                && isBlank(creditUrl)
                && isBlank(license)
                && isBlank(licenseUrl)
                && attributionRequired == null
                && isBlank(attributionText);
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
