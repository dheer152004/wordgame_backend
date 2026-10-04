package com.example.WordGame.modules.quiz.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.WordGame.modules.quiz.QuizDTO.QuizHistoryDTO;
import com.example.WordGame.modules.quiz.QuizDTO.ImageQuizSubmissionRequestDTO;
import com.example.WordGame.modules.quiz.QuizDTO.QuizQuestionResponseDTO;
import com.example.WordGame.modules.quiz.QuizDTO.QuizResultResponseDTO;
import com.example.WordGame.modules.quiz.QuizDTO.QuizSubmissionRequestDTO;

import java.util.List;

public interface QuizService {

    List<QuizQuestionResponseDTO> getTodayQuiz(String userEmail);

            List<QuizQuestionResponseDTO> getTodayImageQuiz(String userEmail);

            List<QuizQuestionResponseDTO> getTodayImageQuiz(String userEmail, String timezoneOffsetMinutes);

            QuizResultResponseDTO submitImageQuiz(String userEmail, ImageQuizSubmissionRequestDTO submission);

        QuizResultResponseDTO submitImageQuiz(
            String userEmail, ImageQuizSubmissionRequestDTO submission, String timezoneOffsetMinutes);

            QuizResultResponseDTO submitQuiz(String userEmail, QuizSubmissionRequestDTO submission);

        QuizResultResponseDTO submitQuiz(
            String userEmail, QuizSubmissionRequestDTO submission, String timezoneOffsetMinutes);

    Page<QuizHistoryDTO> getQuizHistory(String userEmail, Pageable pageable);

    boolean hasCompletedTodayQuiz(String userEmail);

    Object getQuizStats(String userEmail);

    Object getQuizStats(String userEmail, String timezoneOffsetMinutes);
}
