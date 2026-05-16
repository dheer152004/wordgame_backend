package com.example.WordGame.Service;

import com.example.WordGame.DTO.QuizDTO.QuizHistoryDTO;
import com.example.WordGame.DTO.QuizDTO.QuizQuestionResponseDTO;
import com.example.WordGame.DTO.QuizDTO.QuizResultResponseDTO;
import com.example.WordGame.DTO.QuizDTO.QuizSubmissionRequestDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface QuizService {

    List<QuizQuestionResponseDTO> getTodayQuiz(String userEmail);

    QuizResultResponseDTO submitQuiz(String userEmail, QuizSubmissionRequestDTO submission);

    Page<QuizHistoryDTO> getQuizHistory(String userEmail, Pageable pageable);

    boolean hasCompletedTodayQuiz(String userEmail);

    Object getQuizStats(String userEmail);
}
