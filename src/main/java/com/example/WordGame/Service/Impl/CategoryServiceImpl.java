package com.example.WordGame.Service.Impl;


import com.example.WordGame.Config.CloudFlareConfig;
import com.example.WordGame.DTO.CategoryDTO.CategoryRequestDTO;
import com.example.WordGame.DTO.CategoryDTO.CategoryResponseDTO;
import com.example.WordGame.DTO.CategoryDTO.CategoryUpdateDTO;
import com.example.WordGame.Entities.Category;
import com.example.WordGame.Repository.CategoryRepo;
import com.example.WordGame.Repository.WordRepo;
import com.example.WordGame.Service.CategoryService;
import com.example.WordGame.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepo categoryRepo;
    private final WordRepo wordRepo;
    private final ModelMapper modelMapper;
    private final CloudflareImageUploadService imageUploadService;

    @Override
    public List<CategoryResponseDTO> getAllCategories() {
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
    public CategoryResponseDTO getCategoryById(Long id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ApiException("Category not found with id: " + id));

        CategoryResponseDTO dto = modelMapper.map(category, CategoryResponseDTO.class);
        dto.setWordCount(wordRepo.countByCategory(category));
        return dto;
    }

    @Override
    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        // Check if category already exists
        if (categoryRepo.findByName(request.getName()).isPresent()) {
            throw new ApiException("Category already exists with name: " + request.getName());
        }

        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setIsActive(true);
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        // Upload image to Cloudflare R2
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            try {
                String imageUrl = imageUploadService.uploadImage(request.getImage(), "categories");
                category.setImageUrl(imageUrl);
            } catch (Exception e) {
                throw new ApiException("Failed to upload image: " + e.getMessage());
            }
        }

        Category savedCategory = categoryRepo.save(category);

        CategoryResponseDTO response = modelMapper.map(savedCategory, CategoryResponseDTO.class);
        response.setWordCount(0L);
        return response;
    }

    @Override
    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryUpdateDTO request) {
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

        // Upload new image to Cloudflare R2 if provided
        if (request.getImage() != null && !request.getImage().isEmpty()) {
            // Delete old image from Cloudflare R2
            if (category.getImageUrl() != null) {
                imageUploadService.deleteImage(category.getImageUrl());
            }
            try {
                String imageUrl = imageUploadService.uploadImage(request.getImage(), "categories");
                category.setImageUrl(imageUrl);
            } catch (Exception e) {
                throw new ApiException("Failed to upload image: " + e.getMessage());
            }
        }

        category.setUpdatedAt(LocalDateTime.now());
        Category updatedCategory = categoryRepo.save(category);

        CategoryResponseDTO response = modelMapper.map(updatedCategory, CategoryResponseDTO.class);
        response.setWordCount(wordRepo.countByCategory(updatedCategory));
        return response;
    }

    @Override
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new ApiException("Category not found with id: " + id));

        // Check if category has words
        long wordCount = wordRepo.countByCategory(category);
        if (wordCount > 0) {
            throw new ApiException("Cannot delete category with " + wordCount + " words. Delete or move words first.");
        }

        // Delete category image from Cloudflare R2
        if (category.getImageUrl() != null) {
            imageUploadService.deleteImage(category.getImageUrl());
        }

        categoryRepo.delete(category);
    }

    @Override
    @Transactional
    public CategoryResponseDTO toggleCategoryStatus(Long id) {
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