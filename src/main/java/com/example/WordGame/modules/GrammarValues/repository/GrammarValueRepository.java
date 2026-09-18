package com.example.WordGame.modules.GrammarValues.repository;

import com.example.WordGame.modules.GrammarValues.entity.GrammarValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface GrammarValueRepository extends JpaRepository<GrammarValue, Long> {

	@Query("select coalesce(max(g.displayOrder), 0) from GrammarValue g where g.grammarCategory.id = :grammarCategoryId")
	Long findMaxDisplayOrderByGrammarCategoryId(@Param("grammarCategoryId") Long grammarCategoryId);
}
