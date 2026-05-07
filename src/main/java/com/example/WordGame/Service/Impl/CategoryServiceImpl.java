package com.example.WordGame.Service;

import com.example.WordGame.DTO.Word.CategoryResponseDTO;
import com.example.WordGame.Entities.Category;
import com.example.WordGame.Repository.CategoryRepo;
import com.example.WordGame.Repository.WordRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepo categoryRepo;
    private final WordRepo wordRepo;
    private final ModelMapper modelMapper;

    @Override
    public List<CategoryResponseDTO> getAllCategories() {
        List<Category> categories = categoryRepo.findAll();

        return categories.stream()
                .map(category -> {
                    CategoryResponseDTO dto = modelMapper.map(category, CategoryResponseDTO.class);
                    long wordCount = wordRepo.countByCategory(category);
                    dto.setWordCount(wordCount);
                    return dto;
                })
                .collect(Collectors.toList());
    }
}