package com.example.WordGame.Repository;

import com.example.WordGame.Entities.WordRelation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WordRelationRepo extends JpaRepository<WordRelation, Long> {
    List<WordRelation> findByWordId(Long wordId);

    List<WordRelation> findByRelatedWordId(Long relatedWordId);
}