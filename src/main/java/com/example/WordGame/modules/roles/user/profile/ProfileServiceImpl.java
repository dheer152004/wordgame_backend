package com.example.WordGame.modules.roles.user.profile;

// import com.example.WordGame.Repository.*;
import com.example.WordGame.Service.ImageStorageService;
import com.example.WordGame.exceptions.ApiException;
import com.example.WordGame.modules.quiz.repository.QuizAttemptRepository;
import com.example.WordGame.modules.roles.user.Entities.User;
import com.example.WordGame.modules.roles.user.Entities.UserProfile;
import com.example.WordGame.modules.roles.user.UserProfileDTO.ChangePasswordRequestDTO;
import com.example.WordGame.modules.roles.user.UserProfileDTO.ProfileResponseDTO;
import com.example.WordGame.modules.roles.user.UserProfileDTO.ProfileUpdateRequestDTO;
import com.example.WordGame.modules.roles.user.UserProfileDTO.StreakResponseDTO;
import com.example.WordGame.modules.roles.user.repository.UserRepository;
import com.example.WordGame.modules.roles.repository.LeaderboardCacheRepository;
import com.example.WordGame.modules.savedwords.repository.UserSavedWordRepository;
import com.example.WordGame.modules.userConsent.repository.UserConsentRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProfileServiceImpl implements ProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;
    private final UserSavedWordRepository savedWordRepository;
    private final QuizAttemptRepository quizAttemptRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageStorageService imageUploadService;
    private final UserConsentRepository userConsentRepository;
    private final LeaderboardCacheRepository leaderboardCacheRepository;

    @Override
    public ProfileResponseDTO getProfile(String userEmail) {
        log.info("📋 Fetching profile for user: {}", userEmail);

        User user = getUserByEmail(userEmail);
        UserProfile profile = getOrCreateProfile(user);

        // Calculate statistics
        Long totalSavedWords = savedWordRepository.countByUser(user);
        Integer totalQuizzes = Math.toIntExact(quizAttemptRepository.countByUser(user));
        Double avgScore = quizAttemptRepository.getAverageScoreByUser(user);

        // Calculate XP progress
        int xpInCurrentLevel = user.getTotalXp() % 100;
        int xpToNextLevel = 100 - xpInCurrentLevel;
        int levelProgress = xpInCurrentLevel;

        // Calculate words mastered (words with 3+ correct answers in quiz)
        Integer wordsMastered = calculateWordsMastered(user);

        return ProfileResponseDTO.builder()
                // Basic Info
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(profile.getDisplayName() != null ? profile.getDisplayName() : user.getDisplayName())
                .avatarUrl(profile.getAvatarUrl() != null ? profile.getAvatarUrl() : user.getAvatarUrl())
                .bio(profile.getBio() != null ? profile.getBio() : user.getBio())
                // .phoneNumber(profile.getPhoneNumber())
                .dateOfBirth(profile.getDateOfBirth())
                .location(profile.getLocation())
                // .website(profile.getWebsite())

                // Social Links
                // .socialInstagram(profile.getSocialInstagram())
                // .socialTwitter(profile.getSocialTwitter())
                // .socialLinkedin(profile.getSocialLinkedin())
                // Gamification Stats
                .totalXp(user.getTotalXp())
                .level(user.getLevel())
                .currentStreak(user.getCurrentStreak())
                .longestStreak(user.getLongestStreak())
                .xpToNextLevel(xpToNextLevel)
                .levelProgress(levelProgress)
                // Learning Stats
                .totalWordsSaved(totalSavedWords)
                .totalQuizzesCompleted(totalQuizzes != null ? totalQuizzes : 0)
                .averageQuizScore(avgScore != null ? Math.round(avgScore * 100.0) / 100.0 : 0.0)
                .wordsMastered(wordsMastered)
                // Activity
                .lastActive(user.getLastActive())
                .createdAt(user.getCreatedAt())
                .lastQuizDate(user.getLastQuizDate())
                // Badges
                .recentBadges(getUserBadges(user, totalSavedWords, totalQuizzes))
                .build();
    }

    @Override
    public StreakResponseDTO getStreakInfo(String userEmail) {
        log.info("🔥 Fetching streak info for user: {}", userEmail);

        User user = getUserByEmail(userEmail);

        return StreakResponseDTO.builder()
                .currentStreak(user.getCurrentStreak())
                .longestStreak(user.getLongestStreak())
                .build();
    }

    @Override
    @Transactional
    public ProfileResponseDTO updateProfile(String userEmail, ProfileUpdateRequestDTO request) {
        log.info("✏️ Updating profile for user: {}", userEmail);

        User user = getUserByEmail(userEmail);
        UserProfile profile = getOrCreateProfile(user);

        // Update basic info
        if (request.getDisplayName() != null && !request.getDisplayName().isEmpty()) {
            profile.setDisplayName(request.getDisplayName());
            user.setDisplayName(request.getDisplayName());
        }

        if (request.getBio() != null) {
            profile.setBio(request.getBio());
            user.setBio(request.getBio());
        }

        if (request.getPhoneNumber() != null) {
            profile.setPhoneNumber(request.getPhoneNumber());
        }

        if (request.getDateOfBirth() != null) {
            profile.setDateOfBirth(request.getDateOfBirth());
        }

        if (request.getLocation() != null) {
            profile.setLocation(request.getLocation());
        }

        // if (request.getWebsite() != null) {
        //     profile.setWebsite(request.getWebsite());
        // }

        // // Update social links
        // if (request.getSocialInstagram() != null) {
        //     profile.setSocialInstagram(request.getSocialInstagram());
        // }

        // if (request.getSocialTwitter() != null) {
        //     profile.setSocialTwitter(request.getSocialTwitter());
        // }

        // if (request.getSocialLinkedin() != null) {
        //     profile.setSocialLinkedin(request.getSocialLinkedin());
        // }

        profile.setUpdatedAt(LocalDateTime.now());
        user.setUpdatedAt(LocalDateTime.now());

        userRepository.save(user);
        userProfileRepository.save(profile);

        log.info("✅ Profile updated successfully for user: {}", userEmail);

        return getProfile(userEmail);
    }

    @Override
    @Transactional
    public ProfileResponseDTO uploadAvatar(String userEmail, MultipartFile avatarFile) {
        log.info("📸 Uploading avatar for user: {}", userEmail);

        User user = getUserByEmail(userEmail);
        UserProfile profile = getOrCreateProfile(user);

        if (avatarFile == null || avatarFile.isEmpty()) {
            throw new ApiException("Avatar file is required");
        }

        // Validate file type
        String contentType = avatarFile.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new ApiException("Only image files are allowed");
        }

        // Validate file size (max 2MB)
        if (avatarFile.getSize() > 2 * 1024 * 1024) {
            throw new ApiException("Avatar file size must be less than 2MB");
        }

        try {
            // Delete old avatar if exists and not default
            if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().contains("ui-avatars.com")) {
                imageUploadService.deleteImage(profile.getAvatarUrl());
            }

            String avatarUrl = imageUploadService.uploadImage(avatarFile, "avatars");
            profile.setAvatarUrl(avatarUrl);
            user.setAvatarUrl(avatarUrl);
            profile.setUpdatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);
            userProfileRepository.save(profile);

            log.info("✅ Avatar uploaded successfully for user: {}", userEmail);

        } catch (Exception e) {
            throw new ApiException("Failed to upload avatar: " + e.getMessage());
        }

        return getProfile(userEmail);
    }

    @Override
    @Transactional
    public ProfileResponseDTO removeAvatar(String userEmail) {
        log.info("🗑️ Removing avatar for user: {}", userEmail);

        User user = getUserByEmail(userEmail);
        UserProfile profile = getOrCreateProfile(user);

        try {
            // Delete old avatar from storage if not default
            if (profile.getAvatarUrl() != null && !profile.getAvatarUrl().contains("ui-avatars.com")) {
                imageUploadService.deleteImage(profile.getAvatarUrl());
            }

            // Set to default avatar (UI Avatars)
            String defaultAvatarUrl = "https://ui-avatars.com/api/?background=random&name=" + user.getUsername();
            profile.setAvatarUrl(defaultAvatarUrl);
            user.setAvatarUrl(defaultAvatarUrl);
            profile.setUpdatedAt(LocalDateTime.now());
            user.setUpdatedAt(LocalDateTime.now());

            userRepository.save(user);
            userProfileRepository.save(profile);

            log.info("✅ Avatar removed successfully for user: {}", userEmail);

        } catch (Exception e) {
            throw new ApiException("Failed to remove avatar: " + e.getMessage());
        }

        return getProfile(userEmail);
    }

    @Override
    @Transactional
    public void deleteProfile(String userEmail) {
        log.info("🗑️ Deleting profile for user: {}", userEmail);

        User user = getUserByEmail(userEmail);

        // These associations are not cascaded from User. Shared content and
        // all image files are intentionally left untouched.
        userConsentRepository.deleteByUser(user);
        leaderboardCacheRepository.deleteByUser(user);
        userRepository.delete(user);

        log.info("✅ Profile deleted successfully for user: {}", userEmail);
    }

    @Override
    @Transactional
    public void changePassword(String userEmail, ChangePasswordRequestDTO request) {
        log.info("🔐 Changing password for user: {}", userEmail);

        // Validate passwords match
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new ApiException("New password and confirm password do not match");
        }

        // Validate password length
        if (request.getNewPassword().length() < 6) {
            throw new ApiException("New password must be at least 6 characters");
        }

        User user = getUserByEmail(userEmail);

        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new ApiException("Current password is incorrect");
        }

        // Check if new password is same as old
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new ApiException("New password must be different from current password");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);

        log.info("✅ Password changed successfully for user: {}", userEmail);
    }

    // ============ PRIVATE HELPER METHODS ============

    private User getUserByEmail(String email) {
        return userRepository.findByUsername(email)
                .orElseThrow(() -> new ApiException("User not found with email: " + email));
    }

    private UserProfile getOrCreateProfile(User user) {
        return userProfileRepository.findByUser(user)
                .orElseGet(() -> {
                    UserProfile newProfile = new UserProfile();
                    newProfile.setUser(user);
                    newProfile.setDisplayName(user.getDisplayName());
                    newProfile.setAvatarUrl(user.getAvatarUrl());
                    newProfile.setBio(user.getBio());
                    newProfile.setCreatedAt(LocalDateTime.now());
                    newProfile.setUpdatedAt(LocalDateTime.now());
                    return userProfileRepository.save(newProfile);
                });
    }

    private Integer calculateWordsMastered(User user) {
        // A word is "mastered" if user got it correct in quiz 3+ times
        // This is a simplified calculation
        Long savedCount = savedWordRepository.countByUser(user);
        if (savedCount == 0) return 0;

        // Assume 20% of saved words are mastered
        return (int) (savedCount * 0.2);
    }

    private List<String> getUserBadges(User user, Long savedWords, Integer quizzes) {
        List<String> badges = new ArrayList<>();

        // XP Badges
        if (user.getTotalXp() >= 100) badges.add("🏆 Novice Learner");
        if (user.getTotalXp() >= 500) badges.add("🏆 Intermediate Learner");
        if (user.getTotalXp() >= 1000) badges.add("🏆 Advanced Learner");
        if (user.getTotalXp() >= 5000) badges.add("🏆 Word Master");

        // Streak Badges
        if (user.getCurrentStreak() >= 7) badges.add("🔥 7 Day Streak");
        if (user.getCurrentStreak() >= 30) badges.add("🔥 30 Day Streak");
        if (user.getCurrentStreak() >= 100) badges.add("🔥 100 Day Streak");

        // Saved Words Badges
        if (savedWords >= 10) badges.add("📚 Bookworm (10 words)");
        if (savedWords >= 50) badges.add("📚 Scholar (50 words)");
        if (savedWords >= 100) badges.add("📚 Lexicon Master (100 words)");

        // Quiz Badges
        if (quizzes != null && quizzes >= 10) badges.add("🎯 Quiz Enthusiast");
        if (quizzes != null && quizzes >= 50) badges.add("🎯 Quiz Champion");

        // Level Badges
        if (user.getLevel() >= 5) badges.add("⭐ Level 5 Achieved");
        if (user.getLevel() >= 10) badges.add("⭐ Level 10 Achieved");

        // Perfect Score Badge (if average score is 100%)
        Double avgScore = quizAttemptRepository.getAverageScoreByUser(user);
        if (avgScore != null && avgScore == 100.0 && quizzes != null && quizzes > 0) {
            badges.add("💯 Perfect Scorer");
        }

        if (badges.isEmpty()) {
            badges.add("🌱 New Learner");
        }

        // Limit to 5 badges
        return badges.size() > 5 ? badges.subList(0, 5) : badges;
    }
}