package com.example.WordGame.modules.legalDocuments.repository;

import com.example.WordGame.modules.legalDocuments.entity.LegalDocument;
import com.example.WordGame.modules.legalDocuments.enums.LegalDocumentType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LegalDocumentRepository extends JpaRepository<LegalDocument, Long> {
    Optional<LegalDocument> findFirstByDocumentTypeAndIsActiveTrueOrderByPublishedAtDescEffectiveFromDesc(LegalDocumentType documentType);

    List<LegalDocument> findAllByOrderByPublishedAtDescEffectiveFromDesc();

    List<LegalDocument> findAllByIsActiveTrueOrderByPublishedAtDescEffectiveFromDesc();
}
