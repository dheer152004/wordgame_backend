package com.example.WordGame.Repository.Quiz;

import com.example.WordGame.Entities.DailyQuiz;
import com.example.WordGame.Entities.QuizAttempt;
import com.example.WordGame.Entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface QuizAttemptRepository extends JpaRepository<QuizAttempt, Long> {

    Page<QuizAttempt> findByUserOrderByCompletedAtDesc(User user, Pageable pageable);

    Optional<QuizAttempt> findByUserAndQuiz(User user, DailyQuiz quiz);

    @Query("SELECT qa FROM QuizAttempt qa WHERE qa.user = :user AND qa.quiz.quizDate = CURRENT_DATE")
    Optional<QuizAttempt> findTodayAttemptByUser(@Param("user") User user);

    @Query("SELECT COUNT(qa) > 0 FROM QuizAttempt qa WHERE qa.user = :user AND qa.quiz.quizDate = CURRENT_DATE AND qa.isCompleted = true")
    boolean hasCompletedTodayQuiz(@Param("user") User user);

    Long countByUser(User user);

    @Query("SELECT AVG(qa.score) FROM QuizAttempt qa WHERE qa.user = :user")
    Double getAverageScoreByUser(@Param("user") User user);

    @Query("SELECT SUM(qa.xpEarned) FROM QuizAttempt qa WHERE qa.user = :user")
    Integer getTotalXpByUser(@Param("user") User user);
}
