package com.example.WordGame.Repository.Quiz;

import com.example.WordGame.Entities.QuizAttempt;
import com.example.WordGame.Entities.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserAnswerRepository extends JpaRepository<UserAnswer, Long> {

    List<UserAnswer> findByAttempt(QuizAttempt attempt);

    @Query("SELECT COUNT(ua) FROM UserAnswer ua WHERE ua.attempt = :attempt AND ua.isCorrect = true")
    Long countCorrectAnswersByAttempt(@Param("attempt") QuizAttempt attempt);
}
