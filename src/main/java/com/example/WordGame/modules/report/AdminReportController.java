package com.example.WordGame.modules.report;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/admin/reports", produces = "application/json")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AdminReportController {

    private final ReportService reportService;

    @GetMapping
    public ResponseEntity<List<ReportResponseDTO>> getAll() {
        return ResponseEntity.ok(reportService.getAllReports());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponseDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }

    @PatchMapping("/{id}/resolve")
    public ResponseEntity<ReportResponseDTO> resolve(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.resolveReport(id));
    }
}
