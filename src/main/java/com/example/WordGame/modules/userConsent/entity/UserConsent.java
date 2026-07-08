package com.example.WordGame.modules.userConsent.entity;

import com.example.WordGame.modules.legalDocuments.entity.LegalDocument;
import com.example.WordGame.modules.roles.user.Entities.User;
import com.example.WordGame.modules.userConsent.enums.UserConsentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "user_consents", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"user_id", "legal_document_id"})
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserConsent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "legal_document_id", nullable = false)
    private LegalDocument legalDocument;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserConsentStatus status = UserConsentStatus.GRANTED;

    @Column(name = "accepted_at")
    private LocalDateTime acceptedAt;

    @Column(name = "accepted_from", length = 20)
    private String acceptedFrom;

    @Column(name = "withdrawn_at")
    private LocalDateTime withdrawnAt;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
        if (this.acceptedAt == null) {
            this.acceptedAt = now;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
