package com.example.WordGame.modules.words;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum PartOfSpeech {
    VERB,
    NOUN,
    ADJECTIVE,
    HELPING_VERB,
    ADVERB,
    PRONOUN,
    PREPOSITION,
    CONJUNCTION,
    INTERJECTION,
    DETERMINER,
    ARTICLE,
    OTHER;

    @JsonCreator
    public static PartOfSpeech fromValue(String value) {
        if (value == null) {
            return null;
        }
        return valueOf(value.trim().toUpperCase().replace(' ', '_'));
    }
}
