package com.example.WordGame.modules.roles.user.profile;

import org.springframework.web.multipart.MultipartFile;

import com.example.WordGame.modules.roles.user.UserProfileDTO.ChangePasswordRequestDTO;
import com.example.WordGame.modules.roles.user.UserProfileDTO.ProfileResponseDTO;
import com.example.WordGame.modules.roles.user.UserProfileDTO.ProfileUpdateRequestDTO;

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
