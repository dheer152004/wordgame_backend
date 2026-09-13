package com.example.WordGame.modules.category.Entities;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.WordGame.modules.genre.Entities.Genre;
import com.example.WordGame.modules.words.Entities.Word;
import com.example.WordGame.modules.category.enums.AgeRating;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "genre_id")
    private Genre genre;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(length = 500)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "age_rating", nullable = false, length = 20)
    private AgeRating ageRating = AgeRating.ALL;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @ManyToMany(mappedBy = "categories")
    private List<Word> words = new ArrayList<>();

    public Category(String name) {
        this.name = name;
    }
}
