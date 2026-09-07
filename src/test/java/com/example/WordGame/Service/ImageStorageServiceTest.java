package com.example.WordGame.Service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class ImageStorageServiceTest {

    @Test
    void storageServiceContractCanBeMockedWithoutACloudProvider() {
        ImageStorageService service = mock(ImageStorageService.class);
        assertNotNull(service);
    }
}
