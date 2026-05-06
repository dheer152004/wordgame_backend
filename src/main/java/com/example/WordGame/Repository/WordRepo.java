package com.example.WordGame.Repository;

import com.example.WordGame.Entities.Word;
import com.example.WordGame.Entities.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WordRepo extends JpaRepository<Word, Long> {
    Page<Word> findByCategory(Category category, Pageable pageable);
    long countByCategory(Category category);
}