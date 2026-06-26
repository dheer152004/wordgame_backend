package com.example.WordGame.modules.report;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ReportReason reason;

    @Column(columnDefinition = "text")
    private String description;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "report_screenshots", joinColumns = @JoinColumn(name = "report_id"))
    @Column(name = "screenshot_url", columnDefinition = "text")
    @Builder.Default
    private List<String> screenshotUrls = new ArrayList<>();

    private LocalDateTime createdAt;

    @Builder.Default
    private boolean isResolved = false;

    // Reporter info
    @Column(name = "reported_by_id")
    private Long reportedById;

    @Column(name = "reported_by_email", length = 255)
    private String reportedByEmail;

    @Column(name = "reported_by_username", length = 100)
    private String reportedByUsername;

    @PrePersist
    public void prePersist() {
        if (createdAt == null) createdAt = LocalDateTime.now();
    }
}
