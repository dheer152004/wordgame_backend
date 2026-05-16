package com.example.WordGame.Service.Impl;

import com.example.WordGame.Config.CloudFlareConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;


import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudflareImageUploadService {

    private final S3Client s3Client;
    private final CloudFlareConfig r2Config;

    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );
    private static final long MAX_FILE_SIZE = 2 * 1024 * 1024; // 2MB

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new RuntimeException("Invalid file type. Allowed: JPEG, PNG, GIF, WEBP");
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new RuntimeException("File too large. Max size: 2MB");
        }

        // Generate unique filename
        String extension = getFileExtension(file.getOriginalFilename());
        String filename = folder + "/" + UUID.randomUUID().toString() + extension;

        // Upload to Cloudflare R2
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(r2Config.getBucketName())
                .key(filename)
                .contentType(contentType)
                .build();

        byte[] fileBytes = file.getBytes();
        s3Client.putObject(request, RequestBody.fromBytes(fileBytes));

        // Return public URL
        String publicUrl = r2Config.getPublicUrl() + "/" + filename;
        log.info("Image uploaded successfully: {}", publicUrl);

        return publicUrl;
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            return;
        }

        try {
            // Extract key from URL
            String key = imageUrl.replace(r2Config.getPublicUrl() + "/", "");

            DeleteObjectRequest request = DeleteObjectRequest.builder()
                    .bucket(r2Config.getBucketName())
                    .key(key)
                    .build();

            s3Client.deleteObject(request);
            log.info("Image deleted successfully: {}", imageUrl);
        } catch (Exception e) {
            log.error("Failed to delete image: {}", e.getMessage());
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}