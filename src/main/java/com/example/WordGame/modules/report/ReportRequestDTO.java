package com.example.WordGame.modules.report;

import lombok.Data;

import java.util.List;

@Data
public class ReportRequestDTO {
    private String reason;
    private String description;
    private List<String> screenshotUrls;
}
