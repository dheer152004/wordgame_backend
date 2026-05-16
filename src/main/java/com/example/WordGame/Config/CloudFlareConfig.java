package com.example.WordGame.Config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@Getter
public class CloudFlareConfig {

        @Value("${cloudflare.r2.access-key-id}")
        private String accessKeyId;

        @Value("${cloudflare.r2.secret-access-key}")
        private String secretAccessKey;

        @Value("${cloudflare.r2.endpoint}")
        private String endpoint;

        @Value("${cloudflare.r2.bucket-name}")
        private String bucketName;

        @Value("${cloudflare.r2.public-url}")
        private String publicUrl;

        @Value("${cloudflare.r2.image-compression-enabled:true}")
        private boolean compressionEnabled;

        @Value("${cloudflare.r2.max-image-size-mb:2}")
        private int maxImageSizeMB;

        @Bean
        public S3Client s3Client() {
            return S3Client.builder()
                    .endpointOverride(URI.create(endpoint))
                    .region(Region.US_EAST_1) // Cloudflare R2 uses this region
                    .credentialsProvider(StaticCredentialsProvider.create(
                            AwsBasicCredentials.create(accessKeyId, secretAccessKey)
                    ))
                    .build();
        }

}
