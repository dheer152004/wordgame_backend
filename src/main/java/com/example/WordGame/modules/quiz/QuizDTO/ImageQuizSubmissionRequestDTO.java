package com.example.WordGame.modules.quiz.QuizDTO;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
public class ImageQuizSubmissionRequestDTO {

    @NotEmpty
    @Valid
    private List<AnswerDTO> answers;

    @Data
    public static class AnswerDTO {
        private Long wordId;
        private String selectedOption;
        private Integer timeTakenMs;
    }
}