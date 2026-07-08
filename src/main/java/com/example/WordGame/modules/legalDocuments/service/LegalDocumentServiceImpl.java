package com.example.WordGame.modules.legalDocuments.service;

import com.example.WordGame.modules.legalDocuments.entity.LegalDocument;
import com.example.WordGame.modules.legalDocuments.enums.LegalDocumentType;
import com.example.WordGame.modules.legalDocuments.repository.LegalDocumentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LegalDocumentServiceImpl implements LegalDocumentService {

    private final LegalDocumentRepository legalDocumentRepository;

    @Override
    public LegalDocument createDocument(LegalDocument document) {
        return legalDocumentRepository.save(document);
    }

    @Override
    public LegalDocument updateDocument(Long id, LegalDocument updatedDocument) {
        LegalDocument existing = legalDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Legal document not found: " + id));

        existing.setDocumentType(updatedDocument.getDocumentType());
        existing.setVersion(updatedDocument.getVersion());
        existing.setTitle(updatedDocument.getTitle());
        existing.setContent(updatedDocument.getContent());
        existing.setEffectiveFrom(updatedDocument.getEffectiveFrom());
        existing.setPublishedAt(updatedDocument.getPublishedAt());
        existing.setIsActive(updatedDocument.getIsActive());

        return legalDocumentRepository.save(existing);
    }

    @Override
    public LegalDocument updateStatus(Long id, Boolean isActive) {
        LegalDocument existing = legalDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Legal document not found: " + id));
        existing.setIsActive(isActive);
        return legalDocumentRepository.save(existing);
    }

    @Override
    public void deleteDocument(Long id) {
        legalDocumentRepository.deleteById(id);
    }

    @Override
    public LegalDocument getDocumentById(Long id) {
        return legalDocumentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Legal document not found: " + id));
    }

    @Override
    public LegalDocument getActiveDocument(LegalDocumentType documentType) {
        return legalDocumentRepository
                .findFirstByDocumentTypeAndIsActiveTrueOrderByPublishedAtDescEffectiveFromDesc(documentType)
                .orElseThrow(() -> new RuntimeException("No active legal document found for type: " + documentType));
    }

    @Override
    public List<LegalDocument> getActiveDocuments() {
        return legalDocumentRepository.findAllByOrderByPublishedAtDescEffectiveFromDesc();
    }
}
