package com.example.WordGame.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "words")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Word {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(unique = true, nullable = false, length = 100)
    private String word;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String meaning;

    @Column(name = "meme_image_url", length = 500)
    private String memeImageUrl;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "share_count")
    private Integer shareCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuizQuestion> quizQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserSavedWord> savedByUsers = new ArrayList<>();

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserShare> shares = new ArrayList<>();
}
