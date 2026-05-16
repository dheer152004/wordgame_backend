package com.example.WordGame.DTO.QuizDTO;

import lombok.Data;
import java.util.*;

@Data
public class QuizSubmissionRequestDTO {
    private List<AnswerDTO> answers;

    @Data
    public static class AnswerDTO {
        private Long questionId;
        private String selectedOption; // 'A', 'B', 'C', 'D'
        private Integer timeTakenMs;
    }
}
