package com.example.WordGame.Service;

import com.example.WordGame.DTO.UserProfileDTO.ChangePasswordRequestDTO;
import com.example.WordGame.DTO.UserProfileDTO.ProfileResponseDTO;
import com.example.WordGame.DTO.UserProfileDTO.ProfileUpdateRequestDTO;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

    // Get user profile
    ProfileResponseDTO getProfile(String userEmail);

    // Update profile (text fields only)
    ProfileResponseDTO updateProfile(String userEmail, ProfileUpdateRequestDTO request);

    // Upload/Change avatar image
    ProfileResponseDTO uploadAvatar(String userEmail, MultipartFile avatarFile);

    // Remove avatar (set to default)
    ProfileResponseDTO removeAvatar(String userEmail);

    // Change password
    void changePassword(String userEmail, ChangePasswordRequestDTO request);
}
