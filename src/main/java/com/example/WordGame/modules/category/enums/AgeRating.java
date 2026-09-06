package com.example.WordGame.modules.category.enums;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum AgeRating {
    ALL(0),
    @JsonProperty("13_PLUS")
    _13_PLUS(13),
    @JsonProperty("16_PLUS")
    _16_PLUS(16),
    @JsonProperty("18_PLUS")
    _18_PLUS(18);

    private final int minimumAge;

    AgeRating(int minimumAge) {
        this.minimumAge = minimumAge;
    }

    public int getMinimumAge() {
        return minimumAge;
    }
}