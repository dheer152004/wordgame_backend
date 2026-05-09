package com.example.WordGame.Security;

import com.example.WordGame.DTO.User.LoginRequest;
import com.example.WordGame.DTO.User.LoginResponseDTO;
import com.example.WordGame.DTO.User.RegisterRequest;
import com.example.WordGame.Entities.User;
import com.example.WordGame.Repository.UserRepository;
import com.example.WordGame.exceptions.ApiException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final AuthUtil authUtil;
    private final PasswordEncoder passwordEncoder;

    public LoginResponseDTO login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String token = authUtil.generateToken(user);

        // Get user
        User userInDB = userRepository.findByUsername(loginRequest.getUsername())
                .orElseThrow(() -> new ApiException("User not found"));

        // Update last login
        userInDB.setLastLogin(LocalDateTime.now());
        userInDB.setLastActive(LocalDateTime.now());
        userRepository.save(userInDB);

        return LoginResponseDTO.builder()
                .token(token)
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .totalXp(user.getTotalXp())
                .level(user.getLevel())
                .currentStreak(user.getCurrentStreak())
                .build();
    }

    public LoginResponseDTO register(RegisterRequest registerRequest) {
        // Check if username exists
        User userInDB = userRepository.findByUsername(registerRequest.getUsername()).orElse(null);

        if(userInDB != null) throw new ApiException("User already exists");
        // Create user
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setDisplayName(registerRequest.getDisplayName() != null ? registerRequest.getDisplayName() : registerRequest.getUsername());
        user.setAvatarUrl("https://ui-avatars.com/api/?background=random&name=" + registerRequest.getUsername());

        userRepository.save(user);

        return login(new LoginRequest(registerRequest.getEmail(), registerRequest.getPassword()));
    }
}
