package com.example.WordGame.Service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ImageStorageServiceTest {

    @Test
    void azureImageUploadServiceShouldImplementGenericStorageContract() {
        ImageStorageService service = new AzureImageUploadService();
        assertNotNull(service);
    }
}
