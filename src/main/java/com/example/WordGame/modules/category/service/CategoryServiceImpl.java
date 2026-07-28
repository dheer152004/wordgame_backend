package com.example.WordGame.modules.category.service;

import com.example.WordGame.Service.ImageStorageService;
import com.example.WordGame.exceptions.ApiException;
import com.example.WordGame.modules.category.CategoryDTO.CategoryRequestDTO;
import com.example.WordGame.modules.category.CategoryDTO.CategoryResponseDTO;
import com.example.WordGame.modules.category.CategoryDTO.CategoryUpdateDTO;
import com.example.WordGame.modules.category.Entities.Category;
import com.example.WordGame.modules.category.repository.CategoryRepo;
import com.example.WordGame.modules.genre.Entities.Genre;
import com.example.WordGame.modules.genre.repository.GenreRepo;
import com.example.WordGame.modules.words.repository.WordRepo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepo categoryRepo;
    private final GenreRepo genreRepo;
    private final WordRepo wordRepo;
    private final ModelMapper modelMapper;
    private final ImageStorageService imageUploadService;

    @Override
    @Cacheable(value = "categories", key = "'all'", unless = "#result == null")
    public List<CategoryResponseDTO> getAllCategories() {
        log.info("📚 CACHE MISS - Fetching all categories from DATABASE");
        List<Category> categories = categoryRepo.findAll();
        return categories.stream()
                .map(this::toResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CategoryResponseDTO> searchCategories(String q, Pageable pageable) {
        if (q == null || q.isBlank()) {
            return new PageImpl<>(List.of(), pageable, 0);
        }

        String query = q.trim();
        Page<Category> categoriesPage = categoryRepo.findByNameContainingIgnoreCase(query, pageable);
        return categoriesPage.map(this::toResponseDto);
    }

    @Override
    @Cacheable(value = "categories", key = "#id", unless = "#result == null")
    public CategoryResponseDTO getCategoryById(Long id) {
        log.info("📚 CACHE MISS - Fetching category by id {} from DATABASE", id);
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ApiException("Category not found with id: " + id));
        return toResponseDto(category);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "words", allEntries = true)
    })
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        log.info("📝 Creating new category: {} - Will clear cache", request.getName());

        Category category = new Category();
        category.setGenre(resolveGenre(request.getGenreId()));
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        String imageUrl = resolveImageUrl(request.getImageUrl(), request.getImage());
        if (imageUrl != null) {
            category.setImageUrl(imageUrl);
        }

        Category savedCategory = categoryRepo.save(category);
        log.info("✅ Category saved with ID: {}", savedCategory.getId());
        return toResponseDto(savedCategory);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "categories", key = "#id"),
            @CacheEvict(value = "categories", key = "'all'"),
            @CacheEvict(value = "words", allEntries = true)
    })
    public CategoryResponseDTO updateCategory(Long id, CategoryUpdateDTO request) {
        log.info("📝 Updating category {} - Will clear cache", id);

        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ApiException("Category not found with id: " + id));

        if (request.getGenreId() != null) {
            category.setGenre(resolveGenre(request.getGenreId()));
        } else if (category.getGenre() == null) {
            category.setGenre(resolveGenre(null));
        }

        if (request.getName() != null) {
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        String imageUrl = resolveImageUrl(request.getImageUrl(), request.getImage());
        if (imageUrl != null) {
            category.setImageUrl(imageUrl);
        }

        category.setUpdatedAt(LocalDateTime.now());
        Category updatedCategory = categoryRepo.save(category);
        log.info("✅ Category updated successfully");
        return toResponseDto(updatedCategory);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "words", allEntries = true)
    })
    public void deleteCategory(Long id) {
        log.info("🗑️ Deleting category {} - Will clear cache", id);

        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ApiException("Category not found with id: " + id));

        long wordCount = wordRepo.countByCategory(category);
        if (wordCount > 0) {
            throw new ApiException("Cannot delete category with " + wordCount + " words. Delete or move words first.");
        }

        if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
            try {
                imageUploadService.deleteImage(category.getImageUrl());
            } catch (Exception e) {
                log.error("Failed to delete image: {}", e.getMessage());
            }
        }

        categoryRepo.delete(category);
        log.info("✅ Category deleted successfully");
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "categories", key = "#id"),
            @CacheEvict(value = "categories", key = "'all'")
    })
    public CategoryResponseDTO toggleCategoryStatus(Long id) {
        log.info("🔄 Toggling status for category {} - Will clear cache", id);

        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ApiException("Category not found with id: " + id));

        category.setIsActive(!category.getIsActive());
        category.setUpdatedAt(LocalDateTime.now());
        Category updatedCategory = categoryRepo.save(category);
        return toResponseDto(updatedCategory);
    }

    private Genre resolveGenre(Long genreId) {
        if (genreId != null) {
            return genreRepo.findById(genreId)
                    .orElseThrow(() -> new ApiException("Genre not found with id: " + genreId));
        }

        return genreRepo.findByName("General")
                .orElseGet(() -> {
                    Genre genre = new Genre();
                    genre.setName("General");
                    genre.setCreatedAt(LocalDateTime.now());
                    genre.setUpdatedAt(LocalDateTime.now());
                    return genreRepo.save(genre);
                });
    }

    private CategoryResponseDTO toResponseDto(Category category) {
        CategoryResponseDTO dto = modelMapper.map(category, CategoryResponseDTO.class);
        dto.setGenreId(category.getGenre() != null ? category.getGenre().getId() : null);
        dto.setGenreName(category.getGenre() != null ? category.getGenre().getName() : null);
        dto.setWordCount(wordRepo.countByCategory(category));
        return dto;
    }

    private String resolveImageUrl(String imageUrl, MultipartFile imageFile) {
        if (imageUrl != null && !imageUrl.isBlank()) {
            return imageUrl;
        }

        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // upload to azure under categories folder
                return imageUploadService.uploadImage(imageFile, "categories");
            } catch (IOException e) {
                log.error("Failed to upload image: {}", e.getMessage());
                return null;
            }
        }

        return null;
    }
}