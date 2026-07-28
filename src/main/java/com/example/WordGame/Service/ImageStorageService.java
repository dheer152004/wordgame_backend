package com.example.WordGame.Service;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface ImageStorageService {

    String uploadImage(MultipartFile file, String folder) throws IOException;

    void deleteImage(String imageUrl);
}
