package com.hotelbooking.service.impl;

import com.hotelbooking.service.ImageUploadService;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudImageUploadService implements ImageUploadService {

    private final String baseUrl;

    public CloudImageUploadService(@Value("${app.cloud.image-base-url:https://cdn.example.com/hotels/}") String baseUrl) {
        this.baseUrl = baseUrl;
    }

    @Override
    public String uploadImage(MultipartFile file) {
        String extension = "";
        String original = file.getOriginalFilename();
        if (original != null && original.contains(".")) {
            extension = original.substring(original.lastIndexOf('.'));
        }
        return baseUrl + UUID.randomUUID() + extension;
    }
}
