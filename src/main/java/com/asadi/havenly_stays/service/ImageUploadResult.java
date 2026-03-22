package com.asadi.havenly_stays.service;

public class ImageUploadResult {
    private final String publicUrl;
    private final String objectPath;

    public ImageUploadResult(String publicUrl, String objectPath) {
        this.publicUrl = publicUrl;
        this.objectPath = objectPath;
    }

    public String getPublicUrl() {
        return publicUrl;
    }

    public String getObjectPath() {
        return objectPath;
    }
}
