package com.example.WordGame.modules.badge.BadgeDTO;

import com.example.WordGame.modules.badge.enums.BadgeRarity;
import com.example.WordGame.modules.badge.enums.BadgeType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Data
public class BadgeResponseDTO {
    private Long badgeId;
    private String badgeName;
    private String description;
    private String iconUrl;
    private MultipartFile image;
    private BadgeType badgeType;
    private BadgeRarity rarity;
    private Boolean isHidden;
    private Boolean isActive;
    private Integer displayOrder;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
