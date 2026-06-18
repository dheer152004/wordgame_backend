package com.example.WordGame.modules.report;

import lombok.Data;

import java.util.List;

@Data
public class ReportResponseDTO {
    private Long id;
    private String reason;
    private String description;
    private List<String> screenshotUrls;
    private String createdAt;
    private boolean isResolved;
}
