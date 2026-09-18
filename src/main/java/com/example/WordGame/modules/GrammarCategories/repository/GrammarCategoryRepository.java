package com.example.WordGame.modules.GrammarCategories.repository;

import com.example.WordGame.modules.GrammarCategories.entity.GrammarCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
// import org.springframework.stereotype.Repository;

// @Repository
public interface GrammarCategoryRepository extends JpaRepository<GrammarCategory, Long> {

	@Query("select coalesce(max(g.displayOrder), 0) from GrammarCategory g where g.language.id = :languageId")
	Long findMaxDisplayOrderByLanguageId(@Param("languageId") Long languageId);
}
