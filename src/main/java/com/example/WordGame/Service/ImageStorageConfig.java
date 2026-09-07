package com.example.WordGame.Service;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Configuration
@EnableConfigurationProperties({ImageStorageProperties.class, S3StorageProperties.class})
public class ImageStorageConfig {

    @Bean
    @ConditionalOnProperty(name = "app.image-storage.provider", havingValue = "s3")
    public ImageStorageService s3ImageStorageService(S3StorageProperties properties) {
        return new S3ImageStorageService(properties);
    }
}
