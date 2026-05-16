package com.example.WordGame.DTO.QuizDTO;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class QuizHistoryDTO {
    private Long attemptId;
    private LocalDate quizDate;
    private String quizTitle;
    private Integer score;
    private Integer totalPossible;
    private BigDecimal percentage;
    private Integer xpEarned;
    private LocalDateTime completedAt;
}
