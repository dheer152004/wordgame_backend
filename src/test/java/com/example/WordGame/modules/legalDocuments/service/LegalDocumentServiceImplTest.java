package com.example.WordGame.modules.legalDocuments.service;

import com.example.WordGame.modules.legalDocuments.entity.LegalDocument;
import com.example.WordGame.modules.legalDocuments.enums.LegalDocumentType;
import com.example.WordGame.modules.legalDocuments.repository.LegalDocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

class LegalDocumentServiceImplTest {

    @Mock
    private LegalDocumentRepository legalDocumentRepository;

    @InjectMocks
    private LegalDocumentServiceImpl legalDocumentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void shouldReturnActiveDocumentForType() {
        LegalDocument document = new LegalDocument();
        document.setDocumentType(LegalDocumentType.PRIVACY_POLICY);
        document.setVersion("1.0");
        document.setTitle("Privacy Policy");
        document.setIsActive(true);

        when(legalDocumentRepository.findFirstByDocumentTypeAndIsActiveTrueOrderByPublishedAtDescEffectiveFromDesc(LegalDocumentType.PRIVACY_POLICY))
                .thenReturn(Optional.of(document));

        LegalDocument result = legalDocumentService.getActiveDocument(LegalDocumentType.PRIVACY_POLICY);

        assertNotNull(result);
        assertEquals("1.0", result.getVersion());
        assertEquals("Privacy Policy", result.getTitle());
    }
}
