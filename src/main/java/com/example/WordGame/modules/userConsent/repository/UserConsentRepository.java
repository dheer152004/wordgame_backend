package com.example.WordGame.modules.userConsent.repository;

import com.example.WordGame.modules.legalDocuments.entity.LegalDocument;
import com.example.WordGame.modules.roles.user.Entities.User;
import com.example.WordGame.modules.userConsent.entity.UserConsent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserConsentRepository extends JpaRepository<UserConsent, Long> {
    Optional<UserConsent> findByUserAndLegalDocument(User user, LegalDocument legalDocument);

    List<UserConsent> findByUserOrderByCreatedAtDesc(User user);

    List<UserConsent> findByUserIdOrderByCreatedAtDesc(Long userId);
}
