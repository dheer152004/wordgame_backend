package com.example.WordGame.modules.report;

import com.example.WordGame.Service.ImageStorageService;
import com.example.WordGame.modules.roles.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ReportServiceImplTest {

    @Mock
    private ReportRepo reportRepo;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ImageStorageService imageUploadService;

    @InjectMocks
    private ReportServiceImpl reportService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void createReportUploadsMultipartScreenshotsAndReturnsTheirUrls() throws Exception {
        MockMultipartFile screenshot = new MockMultipartFile(
                "screenshotFiles",
                "screenshot.png",
                "image/png",
                "image-bytes".getBytes()
        );

        ReportRequestDTO request = new ReportRequestDTO();
        request.setReason("spam");
        request.setDescription("Inappropriate content");
        request.setScreenshotFiles(List.of(screenshot));

        when(imageUploadService.uploadImage(eq(screenshot), eq("reports"))).thenReturn("https://cdn.example.com/reports/screenshot.png");
        when(reportRepo.save(any(Report.class))).thenAnswer(invocation -> {
            Report report = invocation.getArgument(0);
            report.setId(1L);
            return report;
        });

        ReportResponseDTO response = reportService.createReport(request);

        assertEquals(1, response.getScreenshotUrls().size());
        assertEquals("https://cdn.example.com/reports/screenshot.png", response.getScreenshotUrls().get(0));
        verify(imageUploadService).uploadImage(screenshot, "reports");
    }
}
