package com.example.WordGame.modules.legalDocuments.controller;

import com.example.WordGame.modules.legalDocuments.entity.LegalDocument;
import com.example.WordGame.modules.legalDocuments.enums.LegalDocumentType;
import com.example.WordGame.modules.legalDocuments.service.LegalDocumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/legal-documents")
@RequiredArgsConstructor
public class LegalDocumentController {

    private final LegalDocumentService legalDocumentService;

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
    public ResponseEntity<LegalDocument> createDocument(@RequestBody LegalDocument document) {
        return ResponseEntity.ok(legalDocumentService.createDocument(document));
    }

    @PutMapping("/{id}")
    public ResponseEntity<LegalDocument> updateDocument(@PathVariable Long id, @RequestBody LegalDocument document) {
        return ResponseEntity.ok(legalDocumentService.updateDocument(id, document));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id) {
        legalDocumentService.deleteDocument(id);
        return ResponseEntity.noContent().build();
    }
}
