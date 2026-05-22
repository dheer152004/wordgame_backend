package com.example.WordGame.Service.Impl;

import com.example.WordGame.DTO.CategoryDTO.CategoryRequestDTO;
import com.example.WordGame.DTO.CategoryDTO.CategoryResponseDTO;
import com.example.WordGame.DTO.CategoryDTO.CategoryUpdateDTO;
import com.example.WordGame.Entities.Category;
import com.example.WordGame.Repository.CategoryRepo;
import com.example.WordGame.Repository.WordRepo;
import com.example.WordGame.Service.CategoryService;
import com.example.WordGame.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepo categoryRepo;
    private final WordRepo wordRepo;
    private final ModelMapper modelMapper;
    private final AzureImageUploadService imageUploadService;

    @Override
    @Cacheable(value = "categories", key = "'all'", unless = "#result == null")
    public List<CategoryResponseDTO> getAllCategories() {
        log.info("📚 CACHE MISS - Fetching all categories from DATABASE");
        List<Category> categories = categoryRepo.findAll();
        return categories.stream()
                .map(category -> {
                    CategoryResponseDTO dto = modelMapper.map(category, CategoryResponseDTO.class);
                    dto.setWordCount(wordRepo.countByCategory(category));
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Cacheable(value = "categories", key = "#id", unless = "#result == null")
    public CategoryResponseDTO getCategoryById(Long id) {
        log.info("📚 CACHE MISS - Fetching category by id {} from DATABASE", id);
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ApiException("Category not found with id: " + id));

        CategoryResponseDTO dto = modelMapper.map(category, CategoryResponseDTO.class);
        dto.setWordCount(wordRepo.countByCategory(category));
        return dto;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "categories", allEntries = true),
            @CacheEvict(value = "words", allEntries = true)
    })
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        log.info("📝 Creating new category: {} - Will clear cache", request.getName());

        if (categoryRepo.findByName(request.getName()).isPresent()) {
            throw new ApiException("Category already exists with name: " + request.getName());
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                String imageUrl = imageUploadService.uploadImage(request.getImage(), "categories");
                category.setImageUrl(imageUrl);
                log.info("✅ Image uploaded: {}", imageUrl);
            } catch (Exception e) {
                throw new ApiException("Failed to upload image: " + e.getMessage());
            }
        }

        Category savedCategory = categoryRepo.save(category);
        log.info("✅ Category saved with ID: {}", savedCategory.getId());

        CategoryResponseDTO response = modelMapper.map(savedCategory, CategoryResponseDTO.class);
        response.setWordCount(0L);
        return response;
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

        if (request.getName() != null && !request.getName().equals(category.getName())) {
            if (categoryRepo.findByName(request.getName()).isPresent()) {
                throw new ApiException("Category already exists with name: " + request.getName());
            }
            category.setName(request.getName());
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getIsActive() != null) {
            category.setIsActive(request.getIsActive());
        }

        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                if (category.getImageUrl() != null && !category.getImageUrl().isEmpty()) {
                    imageUploadService.deleteImage(category.getImageUrl());
                }
                String imageUrl = imageUploadService.uploadImage(request.getImage(), "categories");
                category.setImageUrl(imageUrl);
            } catch (Exception e) {
                throw new ApiException("Failed to upload image: " + e.getMessage());
            }
        }

        category.setUpdatedAt(LocalDateTime.now());
        Category updatedCategory = categoryRepo.save(category);
        log.info("✅ Category updated successfully");

        CategoryResponseDTO response = modelMapper.map(updatedCategory, CategoryResponseDTO.class);
        response.setWordCount(wordRepo.countByCategory(updatedCategory));
        return response;
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

        CategoryResponseDTO response = modelMapper.map(updatedCategory, CategoryResponseDTO.class);
        response.setWordCount(wordRepo.countByCategory(updatedCategory));
        return response;
    }
}