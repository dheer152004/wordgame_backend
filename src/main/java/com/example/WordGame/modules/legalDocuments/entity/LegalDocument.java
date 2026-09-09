package com.example.WordGame.modules.legalDocuments.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.example.WordGame.modules.legalDocuments.enums.LegalDocumentType;
import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Types;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "legal_documents")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LegalDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "document_type", nullable = false)
    private LegalDocumentType documentType;

    @Column(nullable = false)
    private String version;

    @Column(nullable = false)
    private String title;

    @JdbcTypeCode(Types.LONGVARCHAR)
    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "effective_from")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDate effectiveFrom;

    @Column(name = "published_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime publishedAt;

    @Column(name = "is_active")
    private Boolean isActive = true;

    @Column(name = "created_at", updatable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.publishedAt == null) {
            this.publishedAt = now;
        }
        if (this.isActive == null) {
            this.isActive = true;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
