package com.example.WordGame.modules.legalDocuments.controller;

import com.example.WordGame.modules.legalDocuments.entity.LegalDocument;
import com.example.WordGame.modules.legalDocuments.enums.LegalDocumentType;
import com.example.WordGame.modules.legalDocuments.service.LegalDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/legal-documents")
@RequiredArgsConstructor
public class LegalDocumentController {

    private final LegalDocumentService legalDocumentService;

    @Value("${app.admin.create-secret:}")
    private String adminCreateSecret;

    @GetMapping({"", "/"})
    public List<LegalDocument> getAllActiveDocuments() {
        return legalDocumentService.getActiveDocuments();
    }

    @GetMapping("/{id}")
    public LegalDocument getDocumentById(@PathVariable Long id) {
        return legalDocumentService.getDocumentById(id);
    }

    @GetMapping({"/type/{documentType}", "/types/{documentType}"})
    public LegalDocument getActiveDocument(@PathVariable String documentType) {
        return legalDocumentService.getActiveDocument(LegalDocumentType.valueOf(documentType.toUpperCase()));
    }

    @PostMapping
    public ResponseEntity<LegalDocument> createDocument(@RequestBody LegalDocument document,
                                                        @RequestHeader(value = "X-Admin-Secret", required = false) String secret) {
        // Require admin creation secret to prevent unauthorized creation of legal documents
        if (adminCreateSecret == null || adminCreateSecret.isBlank() || !adminCreateSecret.equals(secret)) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(legalDocumentService.createDocument(document));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LegalDocument> updateDocument(@PathVariable Long id, @RequestBody LegalDocument document) {
        return ResponseEntity.ok(legalDocumentService.updateDocument(id, document));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<LegalDocument> updateStatus(@PathVariable Long id, @RequestBody java.util.Map<String, Boolean> body) {
        Boolean isActive = body.get("isActive");
        if (isActive == null) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(legalDocumentService.updateStatus(id, isActive));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        legalDocumentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
