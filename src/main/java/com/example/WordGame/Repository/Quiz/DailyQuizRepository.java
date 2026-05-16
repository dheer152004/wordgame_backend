package com.example.WordGame.Repository.Quiz;

import com.example.WordGame.Entities.DailyQuiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.Optional;

@Repository
public interface DailyQuizRepository extends JpaRepository<DailyQuiz, Long> {

    Optional<DailyQuiz> findByQuizDate(LocalDate quizDate);

    @Query("SELECT dq FROM DailyQuiz dq WHERE dq.quizDate = CURRENT_DATE")
    Optional<DailyQuiz> findTodayQuiz();

    boolean existsByQuizDate(LocalDate quizDate);
}
