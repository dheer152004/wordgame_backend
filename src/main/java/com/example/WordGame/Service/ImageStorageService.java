package com.example.WordGame.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageStorageService {

    String uploadImage(MultipartFile file, String folder) throws IOException;

    String uploadMedia(MultipartFile file, String folder, String mediaType) throws IOException;

    String uploadMedia(MultipartFile file, String folder, String mediaType,
                       String resourceName, Long resourceId, int mediaNumber) throws IOException;

    void deleteImage(String imageUrl);
}
