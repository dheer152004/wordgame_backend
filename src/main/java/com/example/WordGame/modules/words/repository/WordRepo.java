package com.example.WordGame.modules.words.repository;

import com.example.WordGame.modules.category.Entities.Category;
import com.example.WordGame.modules.words.Entities.Word;
import com.example.WordGame.modules.words.QuizMode;
import com.example.WordGame.modules.category.enums.AgeRating;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WordRepo extends JpaRepository<Word, Long> {

    Page<Word> findByCategoriesContaining(Category category, Pageable pageable);

        @Query("SELECT w FROM Word w JOIN w.categories category WHERE category = :category "
            + "AND (category.ageRating IS NULL OR category.ageRating = :allRating "
            + "OR (:age IS NOT NULL AND category.ageRating = :teenRating AND :age >= 13) "
            + "OR (:age IS NOT NULL AND category.ageRating = :sixteenRating AND :age >= 16) "
            + "OR (:age IS NOT NULL AND category.ageRating = :adultRating AND :age >= 18))")
        Page<Word> findByCategoryAndAge(@Param("category") Category category,
                       @Param("age") Integer age,
                       @Param("allRating") AgeRating allRating,
                       @Param("teenRating") AgeRating teenRating,
                       @Param("sixteenRating") AgeRating sixteenRating,
                       @Param("adultRating") AgeRating adultRating,
                       Pageable pageable);

    List<Word> findAllByCategoriesContaining(Category category);

    @Query("SELECT w FROM Word w JOIN w.categories category WHERE category.id = :categoryId")
    List<Word> findAllByCategoryId(@Param("categoryId") Long categoryId);

    long countByCategoriesContaining(Category category);

    @Query("SELECT DISTINCT w FROM Word w JOIN w.quizModes mode WHERE mode = :mode")
    List<Word> findByQuizMode(@Param("mode") QuizMode mode);

    Optional<Word> findByWord(String word);

    // ✅ FOR POSTGRESQL - Use RANDOM() instead of RAND()
    @Query(value = "SELECT * FROM words ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Word> findRandomWords(@Param("limit") int limit);

    // Get all words without category filter
    @Query("SELECT w FROM Word w")
    Page<Word> findAllWords(Pageable pageable);

    // Get total count of all words
    @Query("SELECT COUNT(w) FROM Word w")
    long getTotalWordCount();

    // For PostgreSQL with offset (using RANDOM())
    @Query(value = "SELECT * FROM words ORDER BY RANDOM() LIMIT :limit OFFSET :offset", nativeQuery = true)
    List<Word> getWordsWithOffset(@Param("offset") int offset, @Param("limit") int limit);

    // Get random words excluding a specific ID for PostgreSQL
    @Query(value = "SELECT * FROM words WHERE id != :excludeId ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Word> findRandomWordsExcluding(@Param("excludeId") Long excludeId, @Param("limit") int limit);

    // Alternative: Using TABLESAMPLE for better performance on large tables (PostgreSQL 9.5+)
    @Query(value = "SELECT * FROM words TABLESAMPLE SYSTEM(5) LIMIT :limit", nativeQuery = true)
    List<Word> findRandomWordsSampling(@Param("limit") int limit);

    // Search by word (case-insensitive, partial match)
    Page<Word> findByWordIgnoreCaseContaining(String word, Pageable pageable);
}