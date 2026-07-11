package com.example.WordGame.modules.userConsent.DTO;

import com.example.WordGame.modules.userConsent.enums.UserConsentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserConsentResponse {
    private Long id;
    private Long legalDocumentId;
    private String legalDocumentTitle;
    private String legalDocumentType;
    private UserConsentStatus status;
    private LocalDateTime acceptedAt;
    private String acceptedFrom;
    private LocalDateTime withdrawnAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
