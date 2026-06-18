package com.example.WordGame.modules.report;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/api/report", produces = "application/json")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportService reportService;

    @PostMapping(consumes = "application/json")
    public ResponseEntity<ReportResponseDTO> createReport(@RequestBody ReportRequestDTO request) {
        ReportResponseDTO dto = reportService.createReport(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping
    public ResponseEntity<List<ReportResponseDTO>> getAllReports() {
        List<ReportResponseDTO> list = reportService.getAllReports();
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReportResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(reportService.getReportById(id));
    }
}
