package com.example.WordGame.Repository;

import com.example.WordGame.Entities.Word;
import com.example.WordGame.Entities.Category;
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
    Page<Word> findByCategory(Category category, Pageable pageable);
    // Add this missing method - IMPORTANT!
    Optional<Word> findByWord(String word);

    long countByCategory(Category category);

    // OPTIMIZED: Get random words - only loads what you need
    @Query(value = "SELECT * FROM words ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Word> findRandomWords(@Param("limit") int limit);

    // Get word count efficiently
    @Query("SELECT COUNT(w) FROM Word w")
    long getTotalWordCount();

    // Get random words for wrong options
    @Query(value = "SELECT * FROM words WHERE id != :excludeId ORDER BY RANDOM() LIMIT :limit", nativeQuery = true)
    List<Word> findRandomWordsExcluding(@Param("excludeId") Long excludeId, @Param("limit") int limit);
}