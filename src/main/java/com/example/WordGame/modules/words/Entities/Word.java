package com.example.WordGame.modules.words.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// import com.example.WordGame.Entities.WordRelation;
import com.example.WordGame.modules.category.Entities.Category;
import com.example.WordGame.modules.quiz.Entities.QuizQuestion;
import com.example.WordGame.modules.roles.UserShare;
import com.example.WordGame.modules.savedwords.Entities.UserSavedWord;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

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

    @Column(nullable = false, length = 255)
    private String word;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String meaning;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "images", columnDefinition = "TEXT")
    private String imagesJson;

    @Column(name = "facts", columnDefinition = "TEXT")
    private String factsJson;

    @Column(name = "examples", columnDefinition = "TEXT")
    private String examplesJson;

    @Column(name = "source_credits", columnDefinition = "TEXT")
    private String sourceCreditsJson;

    @Column(name = "also_appears_in", columnDefinition = "TEXT")
    private String alsoAppearsInJson;

    @Column(name = "related_word_ids", columnDefinition = "TEXT")
    private String relatedWordIdsJson;

    @Column(name = "view_count")
    private Integer viewCount = 0;

    @Column(name = "share_count")
    private Integer shareCount = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<QuizQuestion> quizQuestions = new ArrayList<>();

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserSavedWord> savedByUsers = new ArrayList<>();

    @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<UserShare> shares = new ArrayList<>();

    // @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<WordRelation> outgoingRelations = new ArrayList<>();

    // @OneToMany(mappedBy = "relatedWord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<WordRelation> incomingRelations = new ArrayList<>();

    public List<String> getImages() {
        if (this.imagesJson == null || this.imagesJson.isBlank()) return new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(this.imagesJson, new TypeReference<List<String>>(){});
        } catch (Exception e) {
            return List.of(this.imagesJson);
        }
    }

    // public String getFirstImage() {
    //     List<String> imgs = getImages();
    //     return imgs.isEmpty() ? null : imgs.get(0);
    // }

    public void setImages(List<String> images) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.imagesJson = mapper.writeValueAsString(images);
        } catch (Exception e) {
            this.imagesJson = null;
        }
    }

    public List<Long> getRelatedWordIds() {
        if (this.relatedWordIdsJson == null || this.relatedWordIdsJson.isBlank()) return new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.readValue(this.relatedWordIdsJson, new TypeReference<List<Long>>(){});
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void setRelatedWordIds(List<Long> ids) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            this.relatedWordIdsJson = mapper.writeValueAsString(ids);
        } catch (Exception e) {
            this.relatedWordIdsJson = null;
        }
    }
}
