package com.example.WordGame.Entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "word_examples")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class WordExample {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "word_id", nullable = false)
    @JsonIgnore
    private Word word;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String example;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();
}
