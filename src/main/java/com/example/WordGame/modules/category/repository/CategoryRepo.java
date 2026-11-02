package com.example.WordGame.modules.category.repository;

import com.example.WordGame.modules.category.Entities.Category;
import com.example.WordGame.modules.genre.Entities.Genre;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface CategoryRepo extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);

    List<Category> findAllByGenreId(Long genreId);

    Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

    @Query("select category.id from Category category")
    List<Long> findAllCategoryIds();

    long countByGenre(Genre genre);
}