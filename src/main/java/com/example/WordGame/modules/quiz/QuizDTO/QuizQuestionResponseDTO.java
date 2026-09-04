package com.example.WordGame.modules.quiz.QuizDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestionResponseDTO {

    private Long questionId;
    private Long wordId;
    private String word;
    private String imageUrl;
    private String quizMode;
    private List<String> options;
    private Integer points;

}
