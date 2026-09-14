package com.example.WordGame.modules.words.DTO;

import lombok.Data;

@Data
public class DisplayOrderUpdateRequest {
    private Long categoryId;
    private Long displayOrder;
}