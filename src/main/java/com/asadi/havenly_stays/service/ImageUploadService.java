package com.asadi.havenly_stays.service;

import org.springframework.web.multipart.MultipartFile;

public interface ImageUploadService {
    ImageUploadResult uploadImage(MultipartFile file, String objectBaseName);
    void deleteImage(String objectPath);
}
