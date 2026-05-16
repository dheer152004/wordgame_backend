package com.example.WordGame.Repository.Quiz;

import com.example.WordGame.Entities.DailyQuiz;
import com.example.WordGame.Entities.QuizQuestion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuizQuestionRepository extends JpaRepository<QuizQuestion, Long> {

    List<QuizQuestion> findByQuizOrderByOrderNumberAsc(DailyQuiz quiz);

    @Query("SELECT qq FROM QuizQuestion qq WHERE qq.quiz.quizDate = CURRENT_DATE ORDER BY qq.orderNumber")
    List<QuizQuestion> findTodayQuizQuestions();

    long countByQuiz(DailyQuiz quiz);
}
