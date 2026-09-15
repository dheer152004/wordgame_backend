package com.example.WordGame.modules.roles.admin;

import com.example.WordGame.modules.auth.entity.PendingRegistration;
import com.example.WordGame.modules.auth.repository.PendingRegistrationRepository;
import com.example.WordGame.modules.roles.Role;
import com.example.WordGame.modules.roles.user.DTO.RoleUpdateRequest;
import com.example.WordGame.modules.roles.user.Entities.User;
import com.example.WordGame.modules.roles.user.repository.UserRepository;
import com.example.WordGame.modules.roles.admin.Entities.DeletedUser;
import com.example.WordGame.modules.roles.admin.Repositories.DeletedUserRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "/api/admin/users", produces = "application/json")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserRepository userRepository;
    private final PendingRegistrationRepository pendingRegistrationRepository;
    private final DeletedUserRepository deletedUserRepository;

    @PostMapping("/{id}/roles")
    public ResponseEntity<?> updateUserRoles(@PathVariable Long id, @RequestBody RoleUpdateRequest req) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        Set<String> rolesStr = req.getRoles() == null ? Set.of() : req.getRoles();
        Set<Role> roles = new HashSet<>();
        for (String r : rolesStr) {
            try {
                roles.add(Role.valueOf(r.trim().toUpperCase()));
            } catch (Exception ignored) {
                // skip invalid role strings
            }
        }

        if (roles.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("success", false, "message", "No valid roles provided"));
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok(java.util.Map.of("success", true, "message", "Roles updated", "roles", roles));
    }

    @GetMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> listUsersWithDetails() {
        List<AdminUserDetailsDTO> users = userRepository.findAll().stream()
                .map(this::toAdminUserDetails)
                .toList();
        return ResponseEntity.ok(Map.of("total", users.size(), "users", users));
    }

    @GetMapping("/deleted")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> listDeletedUsers() {
        List<DeletedUserDTO> users = deletedUserRepository.findAllByOrderByDeletedAtDesc().stream()
                .map(this::toDeletedUserDetails)
                .toList();
        return ResponseEntity.ok(Map.of("total", users.size(), "users", users));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AdminUserDetailsDTO> getUserDetails(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return ResponseEntity.ok(toAdminUserDetails(user));
    }

    private AdminUserDetailsDTO toAdminUserDetails(User user) {
        return AdminUserDetailsDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .isGuest(user.getIsGuest())
                .isActive(user.getIsActive())
                .emailVerified(user.getEmailVerified())
                .lastLogin(user.getLastLogin())
                .lastActive(user.getLastActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .totalXp(user.getTotalXp())
                .currentStreak(user.getCurrentStreak())
                .longestStreak(user.getLongestStreak())
                .level(user.getLevel())
                .roles(user.getRoles())
                .provider(user.getProvider())
                .build();
    }

            private DeletedUserDTO toDeletedUserDetails(DeletedUser user) {
            List<String> roles = user.getRoles() == null || user.getRoles().isBlank()
                ? List.of()
                : List.of(user.getRoles().split(","));
            return DeletedUserDTO.builder()
                .id(user.getId())
                .originalUserId(user.getOriginalUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .isGuest(user.getIsGuest())
                .emailVerified(user.getEmailVerified())
                .lastLogin(user.getLastLogin())
                .lastActive(user.getLastActive())
                .createdAt(user.getCreatedAt())
                .deletedAt(user.getDeletedAt())
                .deletionReason(user.getDeletionReason())
                .roles(roles)
                .build();
            }

    @GetMapping("/pending-registrations")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<java.util.Map<String, Object>> listPendingRegistrations() {
        List<PendingRegistrationDTO> registrations = pendingRegistrationRepository
                .findAllByVerificationExpiresAtAfterOrderByIdAsc(LocalDateTime.now())
                .stream()
                .map(PendingRegistrationDTO::from)
                .toList();

        return ResponseEntity.ok(java.util.Map.of(
                "total", registrations.size(),
                "registrations", registrations));
    }

            @DeleteMapping("/pending-registrations/{id}")
            @PreAuthorize("hasRole('ADMIN')")
            public ResponseEntity<Map<String, Object>> deletePendingRegistration(@PathVariable Long id) {
            PendingRegistration registration = pendingRegistrationRepository.findById(id)
                .orElse(null);

            if (registration == null) {
                return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "Pending registration not found"));
            }

            if (registration.getVerificationExpiresAt() == null
                || !registration.getVerificationExpiresAt().isAfter(LocalDateTime.now())) {
                return ResponseEntity.status(410).body(Map.of(
                    "success", false,
                    "message", "Pending registration has already expired"));
            }

            pendingRegistrationRepository.delete(registration);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Pending registration deleted successfully",
                "id", id));
            }
}
