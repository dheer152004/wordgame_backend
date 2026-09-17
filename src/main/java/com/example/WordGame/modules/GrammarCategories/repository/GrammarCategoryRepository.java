package com.example.WordGame.modules.GrammarCategories.repository;

import com.example.WordGame.modules.GrammarCategories.entity.GrammarCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrammarCategoryRepository extends JpaRepository<GrammarCategory, Long> {
}
