package com.example.WordGame.Controller;

import com.example.WordGame.DTO.UserProfileDTO.ChangePasswordRequestDTO;
import com.example.WordGame.DTO.UserProfileDTO.ProfileResponseDTO;
import com.example.WordGame.DTO.UserProfileDTO.ProfileUpdateRequestDTO;
import com.example.WordGame.Service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user/profile")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ProfileController {

    private final ProfileService profileService;

    /**
     * 4. GET /api/user/profile
     * Get user profile details with all stats
     */
    @GetMapping
    public ResponseEntity<ProfileResponseDTO> getProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        ProfileResponseDTO profile = profileService.getProfile(userEmail);
        return ResponseEntity.ok(profile);
    }

    /**
     * 5. PUT /api/user/profile
     * Update profile (text fields only - no image)
     */
    @PutMapping
    public ResponseEntity<ProfileResponseDTO> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ProfileUpdateRequestDTO request) {
        String userEmail = userDetails.getUsername();

        // Ensure avatar image is not being sent here
        request.setAvatarImage(null);

        ProfileResponseDTO updatedProfile = profileService.updateProfile(userEmail, request);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * 6. POST /api/user/profile/avatar
     * Upload/Change profile avatar image only
     */
    @PostMapping(value = "/avatar", consumes = {"multipart/form-data"})
    public ResponseEntity<ProfileResponseDTO> uploadAvatar(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam("avatar") MultipartFile avatarFile) {
        String userEmail = userDetails.getUsername();
        ProfileResponseDTO updatedProfile = profileService.uploadAvatar(userEmail, avatarFile);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * 7. DELETE /api/user/profile/avatar
     * Remove profile avatar (set to default)
     */
    @DeleteMapping("/avatar")
    public ResponseEntity<ProfileResponseDTO> deleteAvatar(
            @AuthenticationPrincipal UserDetails userDetails) {
        String userEmail = userDetails.getUsername();
        ProfileResponseDTO updatedProfile = profileService.removeAvatar(userEmail);
        return ResponseEntity.ok(updatedProfile);
    }

    /**
     * 8. PUT /api/user/profile/change-password
     * Change user password
     */
    @PutMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequestDTO request) {
        String userEmail = userDetails.getUsername();
        profileService.changePassword(userEmail, request);

        Map<String, String> response = new HashMap<>();
        response.put("message", "Password changed successfully");
        return ResponseEntity.ok(response);
    }
}