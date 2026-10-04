package com.example.WordGame.modules.quiz.QuizDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
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
    private Integer longestStreak;
    private LocalDate lastActivityDate;
    private Boolean streakUpdatedToday;
    private Boolean milestoneReached;
    private Integer streakMilestone;
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
