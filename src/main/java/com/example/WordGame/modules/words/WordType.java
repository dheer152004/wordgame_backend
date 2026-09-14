package com.example.WordGame.modules.words;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum WordType {
    ACRONYM,
    NORMAL_WORD,
    ABBREVIATION,
    CONTRACTION,
    SHORTENED_WORD,
    BLEND,
    COMPOUND_WORD,
    IDIOM,
    PHRASAL_VERB,
    PROVERB;

    @JsonCreator
    public static WordType fromValue(String value) {
        if (value == null) {
            return null;
        }
        return valueOf(value.trim().toUpperCase().replace(' ', '_'));
    }
}
