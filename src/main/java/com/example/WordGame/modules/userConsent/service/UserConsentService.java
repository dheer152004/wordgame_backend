package com.example.WordGame.modules.userConsent.service;

import com.example.WordGame.modules.userConsent.DTO.UserConsentResponse;

import java.util.List;

public interface UserConsentService {
    com.example.WordGame.modules.userConsent.entity.UserConsent createConsent(Long userId, Long legalDocumentId, String acceptedFrom);

    List<UserConsentResponse> getUserConsents(Long userId);
}
