package com.example.WordGame.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "quiz_attempts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizAttempt {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    private DailyQuiz quiz;

    private Integer score = 0;

    @Column(name = "total_possible", nullable = false)
    private Integer totalPossible;

    private BigDecimal percentage;

    @Column(name = "xp_earned")
    private Integer xpEarned = 0;

    @Column(name = "time_taken_seconds")
    private Integer timeTakenSeconds;

    @Column(name = "is_completed")
    private Boolean isCompleted = false;

    @Column(name = "started_at")
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @OneToMany(mappedBy = "attempt", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserAnswer> answers = new ArrayList<>();
}
