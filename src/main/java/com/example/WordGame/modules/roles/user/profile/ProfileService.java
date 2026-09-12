package com.example.WordGame.modules.roles.user.profile;

import org.springframework.web.multipart.MultipartFile;

import com.example.WordGame.modules.roles.user.UserProfileDTO.ChangePasswordRequestDTO;
import com.example.WordGame.modules.roles.user.UserProfileDTO.DateOfBirthUpdateRequestDTO;
import com.example.WordGame.modules.roles.user.UserProfileDTO.ProfileResponseDTO;
import com.example.WordGame.modules.roles.user.UserProfileDTO.ProfileUpdateRequestDTO;
import com.example.WordGame.modules.roles.user.UserProfileDTO.StreakResponseDTO;

public interface ProfileService {

    // Get user profile
    ProfileResponseDTO getProfile(String userEmail);

    // Update profile (text fields only)
    ProfileResponseDTO updateProfile(String userEmail, ProfileUpdateRequestDTO request);

    ProfileResponseDTO updateDateOfBirth(String userEmail, DateOfBirthUpdateRequestDTO request);

    // Upload/Change avatar image
    ProfileResponseDTO uploadAvatar(String userEmail, MultipartFile avatarFile);

    // Remove avatar (set to default)
    ProfileResponseDTO removeAvatar(String userEmail);

    // Delete the authenticated user's account and user-owned data
    void deleteProfile(String userEmail);

    void deleteProfile(String userEmail, String reason);

    // Change password
    void changePassword(String userEmail, ChangePasswordRequestDTO request);

    // Get streak summary
    StreakResponseDTO getStreakInfo(String userEmail);
}
