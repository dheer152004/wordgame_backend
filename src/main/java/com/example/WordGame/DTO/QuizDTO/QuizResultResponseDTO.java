package com.example.WordGame.DTO.QuizDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class QuizResultResponseDTO {
    private Integer score;
    private Integer totalPossible;
    private BigDecimal percentage;
    private Integer xpEarned;
    private Integer newTotalXp;
    private Integer newLevel;
    private Integer currentStreak;
    private String message;
    private List<QuestionResultDTO> details;

    @Data
    @Builder
    public static class QuestionResultDTO {
        private Long questionId;
        private String word;
        private Boolean isCorrect;
        private String correctAnswer;
        private String yourAnswer;
        private String explanation;
        private Integer pointsEarned;
    }
}
