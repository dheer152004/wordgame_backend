package com.example.WordGame.Service.Impl;

import com.example.WordGame.DTO.GenreDTO.GenreRequestDTO;
import com.example.WordGame.DTO.GenreDTO.GenreResponseDTO;
import com.example.WordGame.DTO.GenreDTO.GenreUpdateDTO;
import com.example.WordGame.Entities.Genre;
import com.example.WordGame.Repository.CategoryRepo;
import com.example.WordGame.Repository.GenreRepo;
import com.example.WordGame.Service.GenreService;
import com.example.WordGame.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenreServiceImpl implements GenreService {

    private final GenreRepo genreRepo;
    private final CategoryRepo categoryRepo;
    private final AzureImageUploadService imageUploadService;

    @Override
    public List<GenreResponseDTO> getAllGenres() {
        return genreRepo.findAll().stream().map(this::toResponseDto).collect(Collectors.toList());
    }

    @Override
    public GenreResponseDTO getGenreById(Long id) {
        Genre genre = genreRepo.findById(id)
                .orElseThrow(() -> new ApiException("Genre not found with id: " + id));
        return toResponseDto(genre);
    }

    @Override
    @Transactional
    public GenreResponseDTO createGenre(GenreRequestDTO request) {
        Genre genre = new Genre();
        genre.setName(request.getName());
        genre.setDescription(request.getDescription());
        genre.setCreatedAt(LocalDateTime.now());
        genre.setUpdatedAt(LocalDateTime.now());

        if (request.getImageUrl() != null && !request.getImageUrl().isBlank()) {
            genre.setImageUrl(request.getImageUrl());
        }

        return toResponseDto(genreRepo.save(genre));
    }

    @Override
    @Transactional
    public GenreResponseDTO updateGenre(Long id, GenreUpdateDTO request) {
        Genre genre = genreRepo.findById(id)
                .orElseThrow(() -> new ApiException("Genre not found with id: " + id));

        if (request.getName() != null) {
            genre.setName(request.getName());
        }

        if (request.getDescription() != null) {
            genre.setDescription(request.getDescription());
        }

        if (request.getImageUrl() != null) {
            genre.setImageUrl(request.getImageUrl());
        }

        genre.setUpdatedAt(LocalDateTime.now());
        return toResponseDto(genreRepo.save(genre));
    }

    @Override
    @Transactional
    public GenreResponseDTO patchGenre(Long id, GenreUpdateDTO request) {
        return updateGenre(id, request);
    }

    @Override
    @Transactional
    public void deleteGenre(Long id) {
        Genre genre = genreRepo.findById(id)
                .orElseThrow(() -> new ApiException("Genre not found with id: " + id));

        long categoryCount = categoryRepo.countByGenre(genre);
        if (categoryCount > 0) {
            throw new ApiException("Cannot delete genre with " + categoryCount + " categories.");
        }

        if (genre.getImageUrl() != null && !genre.getImageUrl().isBlank()) {
            try {
                imageUploadService.deleteImage(genre.getImageUrl());
            } catch (Exception e) {
                log.warn("Failed to delete genre image: {}", e.getMessage());
            }
        }

        genreRepo.delete(genre);
    }

    private GenreResponseDTO toResponseDto(Genre genre) {
        GenreResponseDTO response = new GenreResponseDTO();
        response.setId(genre.getId());
        response.setName(genre.getName());
        response.setImageUrl(genre.getImageUrl());
        response.setDescription(genre.getDescription());
        response.setCategoryCount(categoryRepo.countByGenre(genre));
        return response;
    }
}