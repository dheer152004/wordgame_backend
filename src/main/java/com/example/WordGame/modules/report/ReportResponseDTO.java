package com.example.WordGame.modules.report;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
public class ReportResponseDTO {
    private Long id;
    private String reason;
    private String description;
    private List<String> screenshotUrls;
    private String createdAt;
    private boolean isResolved;
    private ReportMadeBy reportMadeBy;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReportMadeBy {
        private Long id;
        private String email;
        private String username;
    }
}
