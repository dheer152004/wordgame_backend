package com.example.WordGame.modules.genre.service;

import java.util.List;

import com.example.WordGame.modules.genre.GenreDTO.GenreRequestDTO;
import com.example.WordGame.modules.genre.GenreDTO.GenreResponseDTO;
import com.example.WordGame.modules.genre.GenreDTO.GenreUpdateDTO;

public interface GenreService {
    List<GenreResponseDTO> getAllGenres();

    GenreResponseDTO getGenreById(Long id);

    GenreResponseDTO createGenre(GenreRequestDTO request);

    GenreResponseDTO updateGenre(Long id, GenreUpdateDTO request);

    GenreResponseDTO patchGenre(Long id, GenreUpdateDTO request);

    void deleteGenre(Long id);
}