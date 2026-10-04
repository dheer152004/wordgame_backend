package com.example.WordGame.modules.appversion.controller;

import com.example.WordGame.modules.appversion.dto.AppVersionResponse;
import com.example.WordGame.modules.appversion.service.AppVersionPolicyService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppVersionControllerTest {
    @Mock
    private AppVersionPolicyService service;

    @InjectMocks
    private AppVersionController controller;

    @Test
    void publicPolicyIsNotCacheable() {
        when(service.findPublicPolicy("ANDROID")).thenReturn(new AppVersionResponse(
                "android", "2.0.0", "1.5.0", false, "Update", "Update available",
                "Update now", "https://play.google.com/store/apps/details?id=com.example.app"));

        ResponseEntity<AppVersionResponse> response = controller.getVersionPolicy("ANDROID");

        assertEquals("no-store", response.getHeaders().getCacheControl());
    }
}