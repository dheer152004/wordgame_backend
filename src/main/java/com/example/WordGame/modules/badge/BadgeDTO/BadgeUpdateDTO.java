package com.example.WordGame.modules.badge.BadgeDTO;

import com.example.WordGame.modules.badge.enums.BadgeRarity;
import com.example.WordGame.modules.badge.enums.BadgeType;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class BadgeUpdateDTO {
    private String badgeName;
    private String description;
    private String iconUrl;
    private MultipartFile image;
    private BadgeType badgeType;
    private BadgeRarity rarity;
    private Boolean hidden;
    private Boolean active;
    private Integer displayOrder;
}
