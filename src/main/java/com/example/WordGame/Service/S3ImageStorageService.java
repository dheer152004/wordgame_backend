package com.example.WordGame.Service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;
import software.amazon.awssdk.services.s3.model.ObjectCannedACL;
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
        if (file == null || file.isEmpty()) {
            log.warn("Media upload attempted with null or empty file");
            return null;
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith(mediaType)) {
            throw new IOException("Only " + mediaType + " files are allowed. Found: " + contentType);
        }

        String extension = getFileExtension(file.getOriginalFilename());
        String objectKey = folder + "/" + UUID.randomUUID() + extension;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(properties.getBucket())
                    .key(objectKey)
                    .contentType(contentType)
                    .cacheControl("public, max-age=31536000, immutable")
                    .acl(ObjectCannedACL.PUBLIC_READ)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            String baseUrl = properties.getPublicUrl();
            if (baseUrl == null || baseUrl.isBlank()) {
                baseUrl = buildDefaultUrl();
            }

            String imageUrl = baseUrl + "/" + objectKey;
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
        if (props.getAccessKey() == null || props.getAccessKey().isBlank()) {
            throw new IllegalStateException("S3 access key is required when IMAGE_STORAGE_PROVIDER=s3");
        }
        if (props.getSecretKey() == null || props.getSecretKey().isBlank()) {
            throw new IllegalStateException("S3 secret key is required when IMAGE_STORAGE_PROVIDER=s3");
        }
        if (props.getRegion() == null || props.getRegion().isBlank()) {
            throw new IllegalStateException("S3 region is required when IMAGE_STORAGE_PROVIDER=s3");
        }
    }

    private S3Client buildClient(S3StorageProperties props) {
        AwsBasicCredentials credentials = AwsBasicCredentials.create(props.getAccessKey(), props.getSecretKey());

        S3ClientBuilder builder = S3Client.builder()
                .region(Region.of(props.getRegion()))
                .credentialsProvider(StaticCredentialsProvider.create(credentials));

        if (props.getEndpoint() != null && !props.getEndpoint().isBlank()) {
            builder.endpointOverride(URI.create(props.getEndpoint()));
        }

        if (props.isForcePathStyle()) {
            builder.serviceConfiguration(S3Configuration.builder().pathStyleAccessEnabled(true).build());
        }

        return builder.build();
    }

    private String buildDefaultUrl() {
        String endpoint = properties.getEndpoint();
        if (endpoint == null || endpoint.isBlank()) {
            return "https://" + properties.getBucket() + ".s3." + properties.getRegion() + ".amazonaws.com";
        }
        return endpoint + "/" + properties.getBucket();
    }

    private String extractObjectKey(String imageUrl) {
        String baseUrl = properties.getPublicUrl();
        if (baseUrl == null || baseUrl.isBlank()) {
            baseUrl = buildDefaultUrl();
        }
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
}
