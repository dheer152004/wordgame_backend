package com.example.WordGame.modules.report;

import com.example.WordGame.Service.ImageStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ReportRepo reportRepo;
    private final com.example.WordGame.modules.roles.user.repository.UserRepository userRepository;
    private final ImageStorageService imageUploadService;

    private static final DateTimeFormatter DTF = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    @Override
    public ReportResponseDTO createReport(ReportRequestDTO request) {
        Report report = new Report();
        try {
            report.setReason(mapReason(request.getReason()));
        } catch (Exception e) {
            report.setReason(ReportReason.OTHER);
        }
        report.setDescription(request.getDescription());
        report.setScreenshotUrls(resolveScreenshotUrls(request));

        // attach reporter info from security context if available
        try {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated()) {
                String principalName = auth.getName();
                if (principalName != null) {
                    userRepository.findByUsername(principalName).ifPresent(u -> {
                        report.setReportedById(u.getId());
                        report.setReportedByEmail(u.getEmail());
                        report.setReportedByUsername(u.getUsername());
                    });
                }
            }
        } catch (Exception ignored) {}

        Report saved = reportRepo.save(report);
        return toDto(saved);
    }

    @Override
    public List<ReportResponseDTO> getAllReports() {
        return reportRepo.findAll().stream().map(this::toDto).collect(Collectors.toList());
    }

    @Override
    public ReportResponseDTO getReportById(Long id) {
        return reportRepo.findById(id).map(this::toDto)
                .orElseThrow(() -> new RuntimeException("Report not found: " + id));
    }

    @Override
    public ReportResponseDTO resolveReport(Long id) {
        Report r = reportRepo.findById(id).orElseThrow(() -> new RuntimeException("Report not found: " + id));
        r.setResolved(true);
        Report saved = reportRepo.save(r);
        return toDto(saved);
    }

    private ReportResponseDTO toDto(Report r) {
        ReportResponseDTO dto = new ReportResponseDTO();
        dto.setId(r.getId());
        dto.setReason(r.getReason() != null ? r.getReason().name() : null);
        dto.setDescription(r.getDescription());
        dto.setScreenshotUrls(r.getScreenshotUrls());
        dto.setCreatedAt(r.getCreatedAt() != null ? r.getCreatedAt().format(DTF) : null);
        dto.setResolved(r.isResolved());
        if (r.getReportedById() != null || r.getReportedByEmail() != null || r.getReportedByUsername() != null) {
            ReportResponseDTO.ReportMadeBy by = new ReportResponseDTO.ReportMadeBy(r.getReportedById(), r.getReportedByEmail(), r.getReportedByUsername());
            dto.setReportMadeBy(by);
        }
        return dto;
    }

    private List<String> resolveScreenshotUrls(ReportRequestDTO request) {
        List<String> urls = new ArrayList<>();

        if (request.getScreenshotFiles() != null && !request.getScreenshotFiles().isEmpty()) {
            for (MultipartFile file : request.getScreenshotFiles()) {
                if (file == null || file.isEmpty()) {
                    continue;
                }
                try {
                    String uploadedUrl = imageUploadService.uploadImage(file, "reports");
                    if (uploadedUrl != null && !uploadedUrl.isBlank()) {
                        urls.add(uploadedUrl);
                    }
                } catch (IOException e) {
                    throw new RuntimeException("Failed to upload report image: " + e.getMessage(), e);
                }
            }
        }

        return urls.isEmpty() ? null : urls;
    }

    private ReportReason mapReason(String raw) {
        if (raw == null) return ReportReason.OTHER;
        String normalized = raw.trim().toUpperCase().replaceAll("\\s+", "_");
        return ReportReason.valueOf(normalized);
    }
}
