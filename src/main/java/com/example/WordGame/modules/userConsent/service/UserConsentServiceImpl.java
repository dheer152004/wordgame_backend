package com.example.WordGame.modules.userConsent.service;

import com.example.WordGame.exceptions.ApiException;
import com.example.WordGame.modules.legalDocuments.entity.LegalDocument;
import com.example.WordGame.modules.legalDocuments.repository.LegalDocumentRepository;
import com.example.WordGame.modules.roles.user.Entities.User;
import com.example.WordGame.modules.roles.user.repository.UserRepository;
import com.example.WordGame.modules.userConsent.entity.UserConsent;
import com.example.WordGame.modules.userConsent.enums.UserConsentStatus;
import com.example.WordGame.modules.userConsent.repository.UserConsentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import com.example.WordGame.modules.userConsent.DTO.UserConsentResponse;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserConsentServiceImpl implements UserConsentService {

    private final UserConsentRepository userConsentRepository;
    private final UserRepository userRepository;
    private final LegalDocumentRepository legalDocumentRepository;

    @Override
    @Transactional
    public UserConsent createConsent(Long userId, Long legalDocumentId, String acceptedFrom) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));
        LegalDocument legalDocument = legalDocumentRepository.findById(legalDocumentId)
                .orElseThrow(() -> new ApiException("Legal document not found"));

        Optional<UserConsent> existing = userConsentRepository.findByUserAndLegalDocument(user, legalDocument);
        if (existing.isPresent()) {
            UserConsent consent = existing.get();
            consent.setStatus(UserConsentStatus.GRANTED);
            consent.setAcceptedAt(LocalDateTime.now());
            consent.setAcceptedFrom(acceptedFrom);
            consent.setWithdrawnAt(null);
            return userConsentRepository.save(consent);
        }

        UserConsent consent = new UserConsent();
        consent.setUser(user);
        consent.setLegalDocument(legalDocument);
        consent.setStatus(UserConsentStatus.GRANTED);
        consent.setAcceptedAt(LocalDateTime.now());
        consent.setAcceptedFrom(acceptedFrom);
        return userConsentRepository.save(consent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserConsentResponse> getUserConsents(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ApiException("User not found"));
        List<UserConsent> consents = userConsentRepository.findByUserOrderByCreatedAtDesc(user);
        return consents.stream().map(c -> new UserConsentResponse(
                c.getId(),
                c.getLegalDocument() != null ? c.getLegalDocument().getId() : null,
                c.getLegalDocument() != null ? c.getLegalDocument().getTitle() : null,
                c.getLegalDocument() != null && c.getLegalDocument().getDocumentType() != null ? c.getLegalDocument().getDocumentType().name() : null,
                c.getStatus(),
                c.getAcceptedAt(),
                c.getAcceptedFrom(),
                c.getWithdrawnAt(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        )).collect(Collectors.toList());
    }
}
