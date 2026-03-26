package com.asadi.havenly_stays.service.impl;

import com.asadi.havenly_stays.dto.RoomImageResponse;
import com.asadi.havenly_stays.entity.RoomImage;
import com.asadi.havenly_stays.entity.RoomType;
import com.asadi.havenly_stays.exception.ResourceNotFoundException;
import com.asadi.havenly_stays.repository.RoomImageRepository;
import com.asadi.havenly_stays.repository.RoomTypeRepository;
import com.asadi.havenly_stays.service.ImageUploadResult;
import com.asadi.havenly_stays.service.ImageUploadService;
import com.asadi.havenly_stays.service.RoomImageService;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class RoomImageServiceImpl implements RoomImageService {

    private static final Logger logger = LoggerFactory.getLogger(RoomImageServiceImpl.class);

    private final RoomTypeRepository roomTypeRepository;
    private final RoomImageRepository roomImageRepository;
    private final ImageUploadService imageUploadService;

    public RoomImageServiceImpl(RoomTypeRepository roomTypeRepository,
                                RoomImageRepository roomImageRepository,
                                ImageUploadService imageUploadService) {
        this.roomTypeRepository = roomTypeRepository;
        this.roomImageRepository = roomImageRepository;
        this.imageUploadService = imageUploadService;
    }

    @Override
    public RoomImageResponse uploadRoomImage(Long roomTypeId, MultipartFile file, String title, String description,
                                             Boolean isMainImage, Integer displayOrder) {
        logger.info("Uploading room image roomTypeId={}", roomTypeId);
        RoomType roomType = getRoomTypeEntity(roomTypeId);

        if (Boolean.TRUE.equals(isMainImage)) {
            roomImageRepository.findByRoomTypeId(roomTypeId)
                    .forEach(image -> image.setIsMainImage(false));
        }

        RoomImage image = RoomImage.builder()
                .roomTypeId(roomType.getId())
                .title(title)
                .description(description)
                .isMainImage(isMainImage)
                .displayOrder(displayOrder)
                .build();

        RoomImage savedImage = roomImageRepository.save(image);
        String objectBaseName = roomType.getHotelId() + "-" + roomTypeId + "-" + savedImage.getId();
        ImageUploadResult uploadResult = imageUploadService.uploadImage(file, objectBaseName);
        savedImage.setImageUrl(uploadResult.getPublicUrl());
        savedImage.setStoragePath(uploadResult.getObjectPath());
        RoomImage updatedImage = roomImageRepository.save(savedImage);

        return toResponse(updatedImage);
    }

    @Override
    public RoomImageResponse updateRoomImage(Long roomTypeId, Long imageId, MultipartFile file, String title,
                                             String description, Boolean isMainImage, Integer displayOrder) {
        logger.info("Updating room image roomTypeId={}, imageId={}", roomTypeId, imageId);
        RoomType roomType = getRoomTypeEntity(roomTypeId);
        RoomImage image = roomImageRepository.findByIdAndRoomTypeId(imageId, roomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));

        if (Boolean.TRUE.equals(isMainImage)) {
            roomImageRepository.findByRoomTypeId(roomTypeId)
                    .forEach(existing -> existing.setIsMainImage(false));
        }

        if (file != null && !file.isEmpty()) {
            imageUploadService.deleteImage(resolveStoragePath(image));
            String objectBaseName = roomType.getHotelId() + "-" + roomTypeId + "-" + image.getId();
            ImageUploadResult uploadResult = imageUploadService.uploadImage(file, objectBaseName);
            image.setImageUrl(uploadResult.getPublicUrl());
            image.setStoragePath(uploadResult.getObjectPath());
        }

        if (title != null) {
            image.setTitle(title);
        }
        if (description != null) {
            image.setDescription(description);
        }
        if (isMainImage != null) {
            image.setIsMainImage(isMainImage);
        }
        if (displayOrder != null) {
            image.setDisplayOrder(displayOrder);
        }

        RoomImage saved = roomImageRepository.save(image);
        return toResponse(saved);
    }

    @Override
    public void deleteRoomImage(Long roomTypeId, Long imageId) {
        logger.info("Deleting room image roomTypeId={}, imageId={}", roomTypeId, imageId);
        getRoomTypeEntity(roomTypeId);
        RoomImage image = roomImageRepository.findByIdAndRoomTypeId(imageId, roomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));
        imageUploadService.deleteImage(resolveStoragePath(image));
        roomImageRepository.delete(image);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomImageResponse> getImagesByRoomType(Long roomTypeId) {
        logger.info("Fetching room images roomTypeId={}", roomTypeId);
        getRoomTypeEntity(roomTypeId);
        return roomImageRepository.findByRoomTypeId(roomTypeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private RoomType getRoomTypeEntity(Long id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found: " + id));
    }

    private RoomImageResponse toResponse(RoomImage image) {
        return RoomImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .title(image.getTitle())
                .description(image.getDescription())
                .isMainImage(image.getIsMainImage())
                .displayOrder(image.getDisplayOrder())
                .createdAt(image.getCreatedAt())
                .build();
    }

    private String resolveStoragePath(RoomImage image) {
        if (image.getStoragePath() != null && !image.getStoragePath().isBlank()) {
            return image.getStoragePath();
        }
        return image.getImageUrl();
    }
}
