package com.example.WordGame.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_statictics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "total_xp")
    private Integer totalXp = 0;

    @Column(name = "current_streak")
    private Integer currentStreak = 0;

    @Column(name = "longest_streak")
    private Integer longestStreak = 0;

    @Column(name = "total_quizzes_completed")
    private Integer totalQuizzesCompleted = 0;

    @Column(name = "total_questions_answered")
    private Integer totalQuestionsAnswered = 0;

    @Column(name = "total_correct_answers")
    private Integer totalCorrectAnswers = 0;

    @Column(name = "total_words_seen")
    private Integer totalWordsSeen = 0;

    @Column(name = "words_mastered")
    private Integer wordsMastered = 0;

    private Integer level = 1;

    @Column(name = "level_progress")
    private Integer levelProgress = 0;

    @Column(name = "last_quiz_date")
    private LocalDate lastQuizDate;

    @Column(name = "last_word_swipe_date")
    private LocalDate lastWordSwipeDate;

    @Column(name = "total_shares")
    private Integer totalShares = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();
}
