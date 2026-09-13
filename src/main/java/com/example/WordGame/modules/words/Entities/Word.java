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
import com.example.WordGame.modules.words.QuizMode;
import com.example.WordGame.modules.words.PartOfSpeech;
import com.example.WordGame.modules.words.WordType;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
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

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "word_categories",
            joinColumns = @JoinColumn(name = "word_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id"))
    private java.util.Set<Category> categories = new java.util.LinkedHashSet<>();

    @ElementCollection
    @CollectionTable(name = "word_category_display_orders", joinColumns = @JoinColumn(name = "word_id"))
    @MapKeyColumn(name = "category_id")
    @Column(name = "display_order")
    private java.util.Map<Long, Long> categoryDisplayOrders = new java.util.HashMap<>();

    @Column(nullable = false, length = 255)
    private String word;

    @Enumerated(EnumType.STRING)
    @Column(name = "word_type", nullable = false)
    private WordType wordType = WordType.NORMAL_WORD;

    @Column(name = "expanded_form")
    private String expandedForm;

    @Enumerated(EnumType.STRING)
    @Column(name = "part_of_speech")
    private PartOfSpeech partOfSpeech;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String meaning;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "images", columnDefinition = "TEXT")
    private String imagesJson;

    @Column(name = "videos", columnDefinition = "TEXT")
    private String videosJson;

    @Column(name = "audios", columnDefinition = "TEXT")
    private String audiosJson;

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

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "word_quiz_modes", joinColumns = @JoinColumn(name = "word_id"))
    @Enumerated(EnumType.STRING)
    @Column(name = "mode")
    private java.util.Set<QuizMode> quizModes = new java.util.LinkedHashSet<>();

    // @OneToMany(mappedBy = "word", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<WordRelation> outgoingRelations = new ArrayList<>();

    // @OneToMany(mappedBy = "relatedWord", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    // private List<WordRelation> incomingRelations = new ArrayList<>();

    public List<String> getImages() {
        return readMediaUrls(this.imagesJson, "imageUrl");
    }

    public void setCategories(java.util.Set<Category> categories) {
        this.categories = categories == null ? new java.util.LinkedHashSet<>() : categories;
    }

    public long getCategoryDisplayOrder(Long categoryId) {
        if (categoryId != null && categoryDisplayOrders != null && categoryDisplayOrders.containsKey(categoryId)) {
            return categoryDisplayOrders.get(categoryId);
        }
        return Long.MAX_VALUE;
    }

    public void setCategoryDisplayOrder(Long categoryId, Long order) {
        if (categoryDisplayOrders == null) categoryDisplayOrders = new java.util.HashMap<>();
        categoryDisplayOrders.put(categoryId, order);
    }

    public void setCategoryDisplayOrderIfAbsent(Long categoryId, Long order) {
        if (categoryDisplayOrders == null) categoryDisplayOrders = new java.util.HashMap<>();
        categoryDisplayOrders.putIfAbsent(categoryId, order);
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

    public List<String> getVideos() {
        return readMediaUrls(this.videosJson, "videoUrl");
    }

    public void setVideos(List<String> videos) {
        this.videosJson = writeMediaUrls(videos);
    }

    public List<String> getAudios() {
        return readMediaUrls(this.audiosJson, "audioUrl");
    }

    public void setAudios(List<String> audios) {
        this.audiosJson = writeMediaUrls(audios);
    }

    private List<String> readMediaUrls(String json, String objectUrlField) {
        if (json == null || json.isBlank()) return new ArrayList<>();
        try {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode media = mapper.readTree(json);
            if (!media.isArray()) return List.of(json);

            List<String> urls = new ArrayList<>();
            for (JsonNode entry : media) {
                if (entry.isTextual()) {
                    urls.add(entry.asText());
                } else if (entry.isObject() && entry.has(objectUrlField)) {
                    urls.add(entry.get(objectUrlField).asText());
                }
            }
            return urls;
        } catch (Exception e) {
            return List.of(json);
        }
    }

    private String writeMediaUrls(List<String> urls) {
        if (urls == null) return null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            return mapper.writeValueAsString(urls);
        } catch (Exception e) {
            return null;
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
