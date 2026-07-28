package com.example.WordGame.Service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

class S3ImageStorageServiceTest {

    @Test
    void shouldFailFastWhenS3BucketIsMissing() {
        S3StorageProperties properties = new S3StorageProperties();
        properties.setAccessKey("access");
        properties.setSecretKey("secret");
        properties.setRegion("us-east-1");
        properties.setBucket("");

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> new S3ImageStorageService(properties));
        assert exception.getMessage() != null && exception.getMessage().contains("S3 bucket");
    }
}
