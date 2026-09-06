package com.example.WordGame.Service;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobHttpHeaders;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.util.UUID;

@Slf4j
public class AzureImageUploadService implements ImageStorageService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name:klugimagecontainer}")
    private String containerName;

    private BlobContainerClient blobContainerClient;

    @PostConstruct
    public void init() {
        if (connectionString == null || connectionString.isEmpty()) {
            log.warn("Azure Storage connection string is not configured!");
            return;
        }

        try {
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                    .connectionString(connectionString)
                    .buildClient();

            this.blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);

            if (!blobContainerClient.exists()) {
                blobContainerClient.create();
                log.info("Created Azure Storage container: {}", containerName);
            }

            log.info("Azure Storage initialized successfully. Container: {}", containerName);
        } catch (Exception e) {
            log.error("Failed to initialize Azure Storage: {}", e.getMessage());
        }
    }

    public String uploadImage(MultipartFile file, String folder) throws IOException {
        return uploadMedia(file, folder, "image/");
    }

    @Override
    public String uploadMedia(MultipartFile file, String folder, String mediaType) throws IOException {
        if (file == null || file.isEmpty()) {
            log.warn("Media upload attempted with null or empty file");
            return null;
        }

        if (blobContainerClient == null) {
            log.error("Azure Storage not initialized. Cannot upload media.");
            throw new IOException("Azure Storage not configured properly");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith(mediaType)) {
            throw new IOException("Only " + mediaType + " files are allowed. Found: " + contentType);
        }

        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        } else {
            extension = ".jpg";
        }

        // Create path: folder/unique-id.jpg (e.g., "categories/abc-123.jpg")
        String blobPath = folder + "/" + UUID.randomUUID().toString() + extension;
        log.info("Uploading media to: {}", blobPath);

        // Get blob client and upload
        var blobClient = blobContainerClient.getBlobClient(blobPath);

        // Set headers for CDN caching
        BlobHttpHeaders headers = new BlobHttpHeaders()
                .setContentType(contentType)
                .setCacheControl("public, max-age=31536000, immutable");

        // Upload the file
        blobClient.upload(file.getInputStream(), file.getSize(), true);
        blobClient.setHttpHeaders(headers);

        // Return the full URL
        String imageUrl = blobClient.getBlobUrl();
        log.info("Media uploaded successfully: {}", imageUrl);

        return imageUrl;
    }

    public void deleteImage(String imageUrl) {
        if (imageUrl == null || imageUrl.isEmpty()) {
            log.warn("Delete attempted with null or empty URL");
            return;
        }

        if (blobContainerClient == null) {
            log.error("Azure Storage not initialized. Cannot delete image.");
            return;
        }

        try {
            String blobPath = extractBlobPathFromUrl(imageUrl);
            if (blobPath == null || blobPath.isEmpty()) {
                log.warn("Could not extract blob path from URL: {}", imageUrl);
                return;
            }

            var blobClient = blobContainerClient.getBlobClient(blobPath);

            if (blobClient.exists()) {
                blobClient.delete();
                log.info("Deleted image: {}", blobPath);
            } else {
                log.warn("Image not found for deletion: {}", blobPath);
            }
        } catch (Exception e) {
            log.error("Failed to delete image: {}", e.getMessage());
        }
    }

    private String extractBlobPathFromUrl(String imageUrl) {
        // URL format: https://klugstorage.blob.core.windows.net/klugimagecontainer/categories/uuid.jpg
        try {
            // Find the container name in the URL
            String containerPart = "/" + containerName + "/";
            int containerIndex = imageUrl.indexOf(containerPart);

            if (containerIndex != -1) {
                // Extract everything after the container name
                return imageUrl.substring(containerIndex + containerPart.length());
            }

            // Fallback: get everything after last slash
            int lastSlash = imageUrl.lastIndexOf("/");
            if (lastSlash != -1 && lastSlash + 1 < imageUrl.length()) {
                return imageUrl.substring(lastSlash + 1);
            }

            return null;
        } catch (Exception e) {
            log.error("Failed to extract blob path from URL: {}", e.getMessage());
            return null;
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || filename.lastIndexOf(".") == -1) {
            return ".jpg";
        }
        return filename.substring(filename.lastIndexOf("."));
    }
}