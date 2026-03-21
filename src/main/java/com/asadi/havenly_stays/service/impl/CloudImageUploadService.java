package com.asadi.havenly_stays.service.impl;

import com.asadi.havenly_stays.exception.InvalidFileException;
import com.asadi.havenly_stays.service.ImageUploadResult;
import com.asadi.havenly_stays.service.ImageUploadService;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

@Service
public class CloudImageUploadService implements ImageUploadService {

    private static final long MAX_SIZE_BYTES = 5L * 1024 * 1024;
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            MediaType.IMAGE_JPEG_VALUE,
            MediaType.IMAGE_PNG_VALUE,
            "image/webp"
    );
    private static final Map<String, String> EXTENSIONS = Map.of(
            MediaType.IMAGE_JPEG_VALUE, ".jpg",
            MediaType.IMAGE_PNG_VALUE, ".png",
            "image/webp", ".webp"
    );

    private final String supabaseUrl;
    private final String serviceKey;
    private final String bucket;
    private final RestTemplate restTemplate = new RestTemplate();

    public CloudImageUploadService(
            @Value("${app.supabase.url:}") String supabaseUrl,
            @Value("${app.supabase.service-key:}") String serviceKey,
            @Value("${app.supabase.bucket:hotel-images}") String bucket
    ) {
        this.supabaseUrl = supabaseUrl;
        this.serviceKey = serviceKey;
        this.bucket = bucket;
    }

    @Override
    public ImageUploadResult uploadImage(MultipartFile file, String objectBaseName) {
        validateFile(file);

        String contentType = file.getContentType();
        String extension = EXTENSIONS.get(contentType);
        String objectName = buildObjectName(objectBaseName, extension);

        byte[] payload = readBytes(file);
        String uploadUrl = normalizeBaseUrl(supabaseUrl)
                + "/storage/v1/object/" + bucket + "/" + objectName;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(contentType));
        headers.set("Authorization", "Bearer " + serviceKey);
        headers.set("apikey", serviceKey);
        headers.set("x-upsert", "true");

        HttpEntity<byte[]> entity = new HttpEntity<>(payload, headers);
        ResponseEntity<String> response = restTemplate.exchange(
                uploadUrl,
                HttpMethod.POST,
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new IllegalStateException("Supabase upload failed with status " + response.getStatusCode());
        }

        String publicUrl = normalizeBaseUrl(supabaseUrl)
                + "/storage/v1/object/public/" + bucket + "/" + objectName;
        return new ImageUploadResult(publicUrl, objectName);
    }

    @Override
    public void deleteImage(String objectPath) {
        if (objectPath == null || objectPath.isBlank()) {
            return;
        }
        String normalizedPath = extractObjectPath(objectPath);
        if (normalizedPath == null || normalizedPath.isBlank()) {
            return;
        }
        String deleteUrl = normalizeBaseUrl(supabaseUrl)
                + "/storage/v1/object/" + bucket + "/" + normalizedPath;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + serviceKey);
        headers.set("apikey", serviceKey);

        HttpEntity<Void> entity = new HttpEntity<>(headers);
        ResponseEntity<String> response = restTemplate.exchange(
                deleteUrl,
                HttpMethod.DELETE,
                entity,
                String.class
        );

        if (!response.getStatusCode().is2xxSuccessful() && !response.getStatusCode().is4xxClientError()) {
            throw new IllegalStateException("Supabase delete failed with status " + response.getStatusCode());
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("File is required");
        }
        if (file.getSize() > MAX_SIZE_BYTES) {
            throw new InvalidFileException("File size exceeds 5MB");
        }
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType)) {
            throw new InvalidFileException("Unsupported file type");
        }
        if (supabaseUrl == null || supabaseUrl.isBlank() || serviceKey == null || serviceKey.isBlank()) {
            throw new IllegalStateException("Supabase credentials are not configured");
        }
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new InvalidFileException("Failed to read file");
        }
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null) {
            return "";
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }

    private String buildObjectName(String objectBaseName, String extension) {
        String base = objectBaseName == null || objectBaseName.isBlank()
                ? UUID.randomUUID().toString()
                : objectBaseName.trim();
        return base + extension;
    }

    private String extractObjectPath(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        String publicPrefix = normalizeBaseUrl(supabaseUrl)
                + "/storage/v1/object/public/" + bucket + "/";
        if (trimmed.startsWith(publicPrefix)) {
            return trimmed.substring(publicPrefix.length());
        }
        String objectPrefix = normalizeBaseUrl(supabaseUrl)
                + "/storage/v1/object/" + bucket + "/";
        if (trimmed.startsWith(objectPrefix)) {
            return trimmed.substring(objectPrefix.length());
        }
        return trimmed;
    }
}
