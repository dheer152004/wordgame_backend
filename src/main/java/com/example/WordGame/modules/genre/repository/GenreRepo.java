package com.example.WordGame.modules.genre.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.WordGame.modules.genre.Entities.Genre;

import java.util.Optional;

@Repository
public interface GenreRepo extends JpaRepository<Genre, Long> {
    Optional<Genre> findByName(String name);
}