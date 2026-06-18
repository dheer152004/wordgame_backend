package com.example.WordGame.modules.roles.user.UserProfileDTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class ProfileUpdateRequestDTO {
    private String displayName;
    private String bio;
    private String phoneNumber;
    private LocalDate dateOfBirth;
    private String location;
//    private String website;
//    private String socialInstagram;
//    private String socialTwitter;
//    private String socialLinkedin;
    private MultipartFile avatarImage;
}
