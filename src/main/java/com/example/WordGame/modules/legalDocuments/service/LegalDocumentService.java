package com.example.WordGame.modules.legalDocuments.service;

import com.example.WordGame.modules.legalDocuments.entity.LegalDocument;
import com.example.WordGame.modules.legalDocuments.enums.LegalDocumentType;

import java.util.List;

public interface LegalDocumentService {
    LegalDocument createDocument(LegalDocument document);
    LegalDocument updateDocument(Long id, LegalDocument document);
    void deleteDocument(Long id);
    LegalDocument getDocumentById(Long id);
    LegalDocument getActiveDocument(LegalDocumentType documentType);
    List<LegalDocument> getActiveDocuments();
}
