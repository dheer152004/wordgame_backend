package com.example.WordGame.Repository;

import com.example.WordGame.Entities.Word;
import com.example.WordGame.Entities.WordExample;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface WordExampleRepo extends JpaRepository<WordExample, Long> {
    List<WordExample> findByWord(Word word);
}