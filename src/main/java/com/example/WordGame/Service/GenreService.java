package com.example.WordGame.Service;

import com.example.WordGame.DTO.GenreDTO.GenreRequestDTO;
import com.example.WordGame.DTO.GenreDTO.GenreResponseDTO;
import com.example.WordGame.DTO.GenreDTO.GenreUpdateDTO;

import java.util.List;

public interface GenreService {
    List<GenreResponseDTO> getAllGenres();

    GenreResponseDTO getGenreById(Long id);

    GenreResponseDTO createGenre(GenreRequestDTO request);

    GenreResponseDTO updateGenre(Long id, GenreUpdateDTO request);

    GenreResponseDTO patchGenre(Long id, GenreUpdateDTO request);

    void deleteGenre(Long id);
}