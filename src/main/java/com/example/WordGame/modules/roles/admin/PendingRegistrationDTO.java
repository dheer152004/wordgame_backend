package com.example.WordGame.modules.roles.admin;

import com.example.WordGame.modules.auth.entity.PendingRegistration;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class PendingRegistrationDTO {
    private Long id;
    private String username;
    private String email;
    private String displayName;
    private String role;
    private LocalDateTime verificationExpiresAt;
    private String acceptedDocumentIds;
    private String acceptedFrom;

    public static PendingRegistrationDTO from(PendingRegistration registration) {
        return new PendingRegistrationDTO(
                registration.getId(),
                registration.getUsername(),
                registration.getEmail(),
                registration.getDisplayName(),
                registration.getRole(),
                registration.getVerificationExpiresAt(),
                registration.getAcceptedDocumentIds(),
                registration.getAcceptedFrom());
    }
}