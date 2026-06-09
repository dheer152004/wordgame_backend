package com.example.WordGame.Repository;

import com.example.WordGame.Entities.Category;
import com.example.WordGame.Entities.Genre;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);

    List<Category> findAllByGenreId(Long genreId);

    long countByGenre(Genre genre);
}