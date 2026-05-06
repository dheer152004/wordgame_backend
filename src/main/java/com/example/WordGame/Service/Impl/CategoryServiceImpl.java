package com.example.WordGame.Service;

import com.example.WordGame.DTO.CategoryResponseDTO;
import com.example.WordGame.Entities.Category;
import com.example.WordGame.Repository.CategoryRepo;
import com.example.WordGame.Repository.WordRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepo categoryRepo;
    private final WordRepo wordRepo;

    // Get all categories with word count
    @Override
    public List<CategoryResponseDTO> getAllCategories() {
        List<Category> categories = categoryRepo.findAll();

        return categories.stream()
                .map(category -> {
                    long wordCount = wordRepo.countByCategory(category);
                    return new CategoryResponseDTO(
                            category.getId(),
                            category.getName(),
                            wordCount
                    );
                })
                .collect(Collectors.toList());
    }
}