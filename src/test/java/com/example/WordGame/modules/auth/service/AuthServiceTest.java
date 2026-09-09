package com.example.WordGame.modules.auth.service;

import com.example.WordGame.Service.Email.EmailService;
import com.example.WordGame.exceptions.ApiException;
import com.example.WordGame.modules.auth.DTO.LoginRequest;
import com.example.WordGame.modules.auth.DTO.LoginResponseDTO;
import com.example.WordGame.modules.auth.DTO.RegisterRequest;
import com.example.WordGame.modules.auth.repository.AuthUtil;
import com.example.WordGame.modules.roles.Role;
import com.example.WordGame.modules.roles.user.Entities.User;
import com.example.WordGame.modules.roles.user.repository.UserRepository;
import com.example.WordGame.modules.userConsent.service.UserConsentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
// import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Proxy;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthServiceTest {

    private UserRepository userRepository;
    private AuthenticationManager authenticationManager;
    private AuthUtil authUtil;
    private PasswordEncoder passwordEncoder;
    private TokenBlacklistService tokenBlacklistService;
    private UserConsentService userConsentService;
    private EmailService emailService;
    private AuthService authService;
    private User storedUser;
    private Authentication authenticatedUser;

    @BeforeEach
    void setUp() {
        authUtil = new AuthUtil();
        ReflectionTestUtils.setField(authUtil, "secretKey", "test-secret-key-123456789012345678901234567890");
        tokenBlacklistService = new TokenBlacklistService();
        passwordEncoder = new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return "encoded-" + rawPassword;
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return encodedPassword.equals("encoded-" + rawPassword);
            }
        };
        userConsentService = new UserConsentService() {
            @Override
            public com.example.WordGame.modules.userConsent.entity.UserConsent createConsent(Long userId, Long legalDocumentId, String acceptedFrom) {
                return null;
            }

            @Override
            public java.util.List<com.example.WordGame.modules.userConsent.DTO.UserConsentResponse> getUserConsents(Long userId) {
                return java.util.Collections.emptyList();
            }
        };
        emailService = new EmailService(null);
        storedUser = null;
        authenticatedUser = null;

        userRepository = (UserRepository) Proxy.newProxyInstance(
                UserRepository.class.getClassLoader(),
                new Class[]{UserRepository.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "existsByUsername":
                            return false;
                        case "existsByEmail":
                            return false;
                        case "findByUsername":
                            return Optional.ofNullable(storedUser);
                        case "findByEmail":
                            return Optional.ofNullable(storedUser);
                        case "save":
                            User savedUser = (User) args[0];
                            savedUser.setId(savedUser.getId() == null ? 1L : savedUser.getId());
                            storedUser = savedUser;
                            return savedUser;
                        case "findAll":
                            return storedUser == null ? java.util.Collections.emptyList() : java.util.List.of(storedUser);
                        default:
                            if (method.getReturnType().equals(boolean.class)) {
                                return false;
                            }
                            if (method.getReturnType().equals(int.class)) {
                                return 0;
                            }
                            if (method.getReturnType().equals(long.class)) {
                                return 0L;
                            }
                            if (method.getReturnType().equals(Optional.class)) {
                                return Optional.empty();
                            }
                            if (method.getReturnType().equals(java.util.List.class)) {
                                return java.util.Collections.emptyList();
                            }
                            return null;
                    }
                }
        );

        authenticationManager = (AuthenticationManager) Proxy.newProxyInstance(
                AuthenticationManager.class.getClassLoader(),
                new Class[]{AuthenticationManager.class},
                (proxy, method, args) -> {
                    if ("authenticate".equals(method.getName())) {
                        return authenticatedUser;
                    }
                    return null;
                }
        );

        authService = new AuthService(
                userRepository,
                authenticationManager,
                authUtil,
                passwordEncoder,
                tokenBlacklistService,
                userConsentService,
                emailService
        );
        ReflectionTestUtils.setField(authService, "adminCreateSecret", "");
    }

    private static class StubAuthentication implements Authentication {
        private final Object principal;

        private StubAuthentication(Object principal) {
            this.principal = principal;
        }

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return Collections.emptyList();
        }

        @Override
        public Object getCredentials() {
            return null;
        }

        @Override
        public Object getDetails() {
            return null;
        }

        @Override
        public Object getPrincipal() {
            return principal;
        }

        @Override
        public boolean isAuthenticated() {
            return true;
        }

        @Override
        public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
        }

        @Override
        public String getName() {
            return principal instanceof User user ? user.getUsername() : String.valueOf(principal);
        }
    }

    @Test
    void registerShouldSendEmailVerificationForEmailProvider() {
        User authenticatedUser = new User();
        authenticatedUser.setId(1L);
        authenticatedUser.setUsername("tester");
        authenticatedUser.setEmail("tester@example.com");
        authenticatedUser.setRoles(Set.of(Role.USER));
        authenticatedUser.setProvider(com.example.WordGame.modules.auth.Provider.EMAIL);

        this.authenticatedUser = new StubAuthentication(authenticatedUser);

        RegisterRequest request = new RegisterRequest();
        request.setUsername("tester");
        request.setEmail("tester@example.com");
        request.setPassword("password123");

        LoginResponseDTO response = authService.register(request);

        assertFalse(response.isEmailVerified());
    }

    @Test
    void loginShouldRejectUnverifiedEmailUsers() {
        User user = new User();
        user.setId(1L);
        user.setUsername("tester");
        user.setEmail("tester@example.com");
        user.setEmailVerified(false);
        user.setRoles(Set.of(Role.USER));
        user.setProvider(com.example.WordGame.modules.auth.Provider.EMAIL);

        this.authenticatedUser = new StubAuthentication(user);
        this.storedUser = user;

        ApiException exception = assertThrows(ApiException.class,
                () -> authService.login(new LoginRequest("tester", "password123")));

        assertEquals("Please verify your email before logging in", exception.getMessage());
    }

    @Test
    void forgotPasswordShouldCreateResetTokenAndPersistIt() {
        User user = new User();
        user.setId(1L);
        user.setUsername("tester");
        user.setEmail("tester@example.com");
        user.setRoles(Set.of(Role.USER));
        user.setProvider(com.example.WordGame.modules.auth.Provider.EMAIL);
        this.storedUser = user;

        var response = authService.forgotPassword("tester@example.com");

        assertEquals(true, response.get("success"));
        assertEquals(true, user.getPasswordResetToken() != null && !user.getPasswordResetToken().isBlank());
    }

    @Test
    void resendVerificationEmailShouldCreateNewTokenForUnverifiedUser() {
        User user = new User();
        user.setId(1L);
        user.setUsername("tester");
        user.setEmail("tester@example.com");
        user.setEmailVerified(false);
        user.setRoles(Set.of(Role.USER));
        user.setProvider(com.example.WordGame.modules.auth.Provider.EMAIL);
        this.storedUser = user;

        var response = authService.resendVerificationEmail("tester@example.com");

        assertEquals(true, response.get("success"));
        assertEquals(true, user.getEmailVerificationToken() != null && !user.getEmailVerificationToken().isBlank());
    }
}
