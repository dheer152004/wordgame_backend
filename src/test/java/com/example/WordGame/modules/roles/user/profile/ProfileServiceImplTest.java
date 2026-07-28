package com.example.WordGame.modules.roles.user.profile;

import com.example.WordGame.Service.ImageStorageService;
import com.example.WordGame.modules.quiz.repository.QuizAttemptRepository;
import com.example.WordGame.modules.roles.user.Entities.User;
import com.example.WordGame.modules.roles.user.UserProfileDTO.StreakResponseDTO;
import com.example.WordGame.modules.roles.user.repository.UserRepository;
import com.example.WordGame.modules.savedwords.repository.UserSavedWordRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProfileServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @Mock
    private UserSavedWordRepository savedWordRepository;

    @Mock
    private QuizAttemptRepository quizAttemptRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ImageStorageService imageUploadService;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Test
    void getStreakInfoReturnsCurrentAndLongestStreak() {
        User user = new User();
        user.setEmail("test@example.com");
        user.setCurrentStreak(5);
        user.setLongestStreak(12);

        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));

        StreakResponseDTO streak = profileService.getStreakInfo("test@example.com");

        assertEquals(5, streak.getCurrentStreak());
        assertEquals(12, streak.getLongestStreak());
    }
}
