package com.example.WordGame.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Slf4j
public class S3ImageStorageService implements ImageStorageService {

    private final S3Client s3Client;
    private final S3StorageProperties properties;

    @Autowired
    public S3ImageStorageService(S3StorageProperties properties) {
        if (properties == null) {
            throw new IllegalArgumentException("S3 storage properties must not be null");
        }
        validateConfiguration(properties);
        this.properties = properties;
        this.s3Client = buildClient(properties);
    }

    @Override
    public String uploadImage(MultipartFile file, String folder) throws IOException {
        return uploadMedia(file, folder, "image/");
    }

    @Override
    public String uploadMedia(MultipartFile file, String folder, String mediaType) throws IOException {
        return uploadMedia(file, folder, mediaType, "media", null, 1);
    }

    @Override
    public String uploadMedia(MultipartFile file, String folder, String mediaType,
                              String resourceName, Long resourceId, int mediaNumber) throws IOException {
        if (file == null || file.isEmpty()) {
            log.warn("Media upload attempted with null or empty file");
            return null;
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith(mediaType)) {
            throw new IOException("Only " + mediaType + " files are allowed. Found: " + contentType);
        }

        String extension = getFileExtension(file.getOriginalFilename());
        String objectKey;
        if (resourceId != null) {
            String mediaKind = mediaType.replace("/", "").toLowerCase();
            objectKey = folder + "/" + resourceId + "." + mediaKind + "." + mediaNumber + extension;
        } else {
            String objectName = sanitizeName(resourceName);
            objectKey = folder + "/" + objectName + "-" + UUID.randomUUID() + "-media-" + mediaNumber + extension;
        }

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .contentType(contentType)
                    .cacheControl("public, max-age=31536000, immutable")
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String imageUrl = buildDefaultUrl() + "/" + objectKey;
            log.info("Media uploaded successfully to S3: {}", imageUrl);
            return imageUrl;
        } catch (S3Exception e) {
            log.error("Failed to upload image to S3: {}", e.getMessage(), e);
            throw new IOException("S3 upload failed: " + e.getMessage(), e);
        }
    }

    @Override
    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isBlank()) {
            return;
        }

        try {
            String objectKey = extractObjectKey(imageUrl);
            if (objectKey == null || objectKey.isBlank()) {
                return;
            }

            s3Client.deleteObject(builder -> builder.bucket(properties.getBucket()).key(objectKey));
            log.info("Deleted image from S3: {}", objectKey);
        } catch (S3Exception e) {
            log.error("Failed to delete image from S3: {}", e.getMessage(), e);
        }
    }

    private void validateConfiguration(S3StorageProperties props) {
        if (props.getBucket() == null || props.getBucket().isBlank()) {
            throw new IllegalStateException("S3 bucket is required when IMAGE_STORAGE_PROVIDER=s3");
        }
        if (props.getRegion() == null || props.getRegion().isBlank()) {
            throw new IllegalStateException("S3 region is required when IMAGE_STORAGE_PROVIDER=s3");
        }
    }

    private S3Client buildClient(S3StorageProperties props) {
        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(props.getRegion()))
            .credentialsProvider(credentialsProvider(props));

        if (props.getEndpoint() != null && !props.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(props.getEndpoint()));
        }

        if (props.isForcePathStyle()) {
            builder.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }

        return builder.build();
    }

    private AwsCredentialsProvider credentialsProvider(S3StorageProperties props) {
        if (props.getAccessKey() != null && !props.getAccessKey().isBlank()
                && props.getSecretKey() != null && !props.getSecretKey().isBlank()) {
            return StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey()));
        }
        return DefaultCredentialsProvider.create();
    }

    private String buildDefaultUrl() {
        String endpoint = properties.getEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            return "https://" + properties.getBucket() + ".s3." + properties.getRegion() + ".amazonaws.com";
        }
        return endpoint + "/" + properties.getBucket();
    }

    private String extractObjectKey(String imageUrl) {
        String baseUrl = buildDefaultUrl();
        String normalizedBase = baseUrl.replaceAll("/+$", "");
        if (imageUrl.startsWith(normalizedBase + "/")) {
            return imageUrl.substring(normalizedBase.length() + 1);
        }
        int lastSlash = imageUrl.lastIndexOf('/');
        return lastSlash >= 0 ? imageUrl.substring(lastSlash + 1) : null;
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf('.') == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }

    private String sanitizeName(String name) {
        if (name == null || name.isBlank()) {
            return "media";
        }
        return name.trim()
                .replaceAll("[^a-zA-Z0-9_-]+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }
}
