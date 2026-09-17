package com.example.WordGame.modules.GrammarValues.repository;

import com.example.WordGame.modules.GrammarValues.entity.GrammarValue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrammarValueRepository extends JpaRepository<GrammarValue, Long> {
}
