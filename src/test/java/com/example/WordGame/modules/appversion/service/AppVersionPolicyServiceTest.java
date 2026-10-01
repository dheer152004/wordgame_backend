package com.example.WordGame.modules.appversion.service;

import com.example.WordGame.modules.appversion.dto.AdminAppVersionResponse;
import com.example.WordGame.modules.appversion.dto.AppVersionPolicyRequest;
import com.example.WordGame.modules.appversion.entity.AppPlatform;
import com.example.WordGame.modules.appversion.entity.AppVersionPolicy;
import com.example.WordGame.modules.appversion.repository.AppVersionPolicyRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AppVersionPolicyServiceTest {
    @Mock
    private AppVersionPolicyRepository repository;

    @InjectMocks
    private AppVersionPolicyService service;

    @Test
    void createComparesVersionPartsNumerically() {
        when(repository.existsById(AppPlatform.ANDROID)).thenReturn(false);
        when(repository.save(any(AppVersionPolicy.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AdminAppVersionResponse response = service.create(policyRequest("1.10.0", "1.9.0"));

        assertEquals("ANDROID", response.platform());
        assertEquals("1.10.0", response.latestVersion());
    }

    @Test
    void createRejectsMinimumVersionAboveLatest() {
        when(repository.existsById(AppPlatform.ANDROID)).thenReturn(false);

        assertThrows(ResponseStatusException.class,
                () -> service.create(policyRequest("1.4.0", "1.5.0")));
    }

    private AppVersionPolicyRequest policyRequest(String latestVersion, String minimumVersion) {
        return new AppVersionPolicyRequest(
                "ANDROID", latestVersion, minimumVersion, false, "UPDATE AVAILABLE", "Update message",
                "UPDATE NOW", "https://play.google.com/store/apps/details?id=com.nroq.in");
    }
}