package com.example.WordGame.modules.userConsent.service;

import com.example.WordGame.modules.userConsent.entity.UserConsent;

import java.util.List;

public interface UserConsentService {
    UserConsent createConsent(Long userId, Long legalDocumentId, String acceptedFrom);

    List<UserConsent> getUserConsents(Long userId);
}
