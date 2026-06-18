package com.example.WordGame.modules.report;

import java.util.List;

public interface ReportService {
    ReportResponseDTO createReport(ReportRequestDTO request);
    List<ReportResponseDTO> getAllReports();
    ReportResponseDTO getReportById(Long id);
    ReportResponseDTO resolveReport(Long id);
}
