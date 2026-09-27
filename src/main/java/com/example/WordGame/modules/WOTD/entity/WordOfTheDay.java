package com.example.WordGame.modules.WOTD.entity;

import com.example.WordGame.modules.WOTD.enums.WordOfTheDayStatus;
import com.example.WordGame.modules.words.Entities.Word;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "word_of_the_day", uniqueConstraints = {
        @UniqueConstraint(name = "uk_word_of_the_day_publish_on", columnNames = "publish_on")
})
@Data
public class WordOfTheDay {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "word_id", nullable = false)
    private Word word;

    @Column(name = "publish_on", nullable = false)
    private LocalDate publishOn;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private WordOfTheDayStatus status = WordOfTheDayStatus.SCHEDULED;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}