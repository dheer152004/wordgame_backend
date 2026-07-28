package com.example.WordGame.modules.category.repository;

import com.example.WordGame.modules.category.Entities.Category;
import com.example.WordGame.modules.genre.Entities.Genre;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
// import org.springframework.stereotype.Repository;
import java.util.Optional;

// @Repository
public interface CategoryRepo extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);

    List<Category> findAllByGenreId(Long genreId);

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    long countByGenre(Genre genre);
}