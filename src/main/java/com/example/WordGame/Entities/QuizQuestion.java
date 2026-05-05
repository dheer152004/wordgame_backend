package com.example.WordGame.Entities;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "quiz_questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuizQuestion {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_id", nullable = false)
    @JsonIgnore
    private DailyQuiz quiz;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(name = "correct_answer", nullable = false, length = 500)
    private String correctAnswer;

    @Column(name = "option_a", nullable = false, length = 500)
    private String optionA;

    @Column(name = "option_b", nullable = false, length = 500)
    private String optionB;

    @Column(name = "option_c", nullable = false, length = 500)
    private String optionC;

    @Column(name = "option_d", nullable = false, length = 500)
    private String optionD;

    private Integer points = 10;

    @Column(name = "order_number", nullable = false)
    private Integer orderNumber;

    @Column(columnDefinition = "TEXT")
    private String explanation;
}
