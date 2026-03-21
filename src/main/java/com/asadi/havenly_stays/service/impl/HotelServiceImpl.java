package com.asadi.havenly_stays.service.impl;

import com.asadi.havenly_stays.dto.HotelCreateRequest;
import com.asadi.havenly_stays.dto.HotelImageResponse;
import com.asadi.havenly_stays.dto.HotelPetPolicyRequest;
import com.asadi.havenly_stays.dto.HotelPetPolicyResponse;
import com.asadi.havenly_stays.dto.HotelResponse;
import com.asadi.havenly_stays.dto.HotelUpdateRequest;
import com.asadi.havenly_stays.entity.Facility;
import com.asadi.havenly_stays.entity.Hotel;
import com.asadi.havenly_stays.entity.HotelFacility;
import com.asadi.havenly_stays.entity.HotelImage;
import com.asadi.havenly_stays.entity.HotelPetPolicy;
import com.asadi.havenly_stays.exception.ResourceNotFoundException;
import com.asadi.havenly_stays.mapper.HotelMapper;
import com.asadi.havenly_stays.repository.FacilityRepository;
import com.asadi.havenly_stays.repository.HotelImageRepository;
import com.asadi.havenly_stays.repository.HotelRepository;
import com.asadi.havenly_stays.service.HotelService;
import com.asadi.havenly_stays.service.ImageUploadResult;
import com.asadi.havenly_stays.service.ImageUploadService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class HotelServiceImpl implements HotelService {

    private final HotelRepository hotelRepository;
    private final FacilityRepository facilityRepository;
    private final HotelImageRepository hotelImageRepository;
    private final ImageUploadService imageUploadService;
    private final HotelMapper hotelMapper;

    public HotelServiceImpl(HotelRepository hotelRepository,
                            FacilityRepository facilityRepository,
                            HotelImageRepository hotelImageRepository,
                            ImageUploadService imageUploadService,
                            HotelMapper hotelMapper) {
        this.hotelRepository = hotelRepository;
        this.facilityRepository = facilityRepository;
        this.hotelImageRepository = hotelImageRepository;
        this.imageUploadService = imageUploadService;
        this.hotelMapper = hotelMapper;
    }

    @Override
    public HotelResponse createHotel(HotelCreateRequest request) {
        Hotel hotel = hotelMapper.toEntity(request);
        Hotel saved = hotelRepository.save(hotel);
        return hotelMapper.toResponse(saved);
    }

    @Override
    public HotelResponse updateHotel(Long id, HotelUpdateRequest request) {
        Hotel hotel = getHotelEntity(id);
        hotelMapper.applyUpdate(hotel, request);
        Hotel saved = hotelRepository.save(hotel);
        return hotelMapper.toResponse(saved);
    }

    @Override
    public void deleteHotel(Long id) {
        Hotel hotel = getHotelEntity(id);
        hotel.setIsDeleted(true);
        hotel.setIsActive(false);
        hotelRepository.save(hotel);
    }

    @Override
    @Transactional(readOnly = true)
    public HotelResponse getHotelById(Long id) {
        return hotelMapper.toResponse(getHotelEntity(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelResponse> getAllHotels(Pageable pageable) {
        return hotelRepository.findByIsDeletedFalse(pageable)
                .map(hotelMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<HotelResponse> searchHotels(String query, Pageable pageable) {
        if (query == null || query.isBlank()) {
            return getAllHotels(pageable);
        }
        return hotelRepository.findByIsDeletedFalseAndNameContainingIgnoreCase(query.trim(), pageable)
                .map(hotelMapper::toResponse);
    }

    @Override
    public HotelResponse setFacilities(Long hotelId, List<Long> facilityIds) {
        Hotel hotel = getHotelEntity(hotelId);
        Map<Long, Facility> facilityMap = facilityRepository.findAllById(facilityIds).stream()
                .collect(Collectors.toMap(Facility::getId, facility -> facility));

        List<Long> missing = facilityIds.stream()
                .filter(id -> !facilityMap.containsKey(id))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new ResourceNotFoundException("Facilities not found: " + missing);
        }

        hotel.getHotelFacilities().clear();
        facilityMap.values().forEach(facility -> hotel.getHotelFacilities()
                .add(HotelFacility.builder().hotel(hotel).facility(facility).build()));

        Hotel saved = hotelRepository.save(hotel);
        return hotelMapper.toResponse(saved);
    }

    @Override
    public HotelPetPolicyResponse setPetPolicy(Long hotelId, HotelPetPolicyRequest request) {
        Hotel hotel = getHotelEntity(hotelId);
        HotelPetPolicy policy = hotel.getPetPolicy();
        if (policy == null) {
            policy = new HotelPetPolicy();
            policy.setHotel(hotel);
        }

        policy.setPetsAllowed(request.getPetsAllowed());
        policy.setPetFeeType(request.getPetFeeType());
        policy.setPetFee(request.getPetFee());
        policy.setMaxPets(request.getMaxPets());
        policy.setDescription(request.getDescription());

        hotel.setPetPolicy(policy);
        Hotel saved = hotelRepository.save(hotel);
        return hotelMapper.toResponse(saved).getPetPolicy();
    }

    @Override
    public HotelImageResponse uploadHotelImage(Long hotelId, MultipartFile file, String title, String description,
                                               Boolean isMainImage, Integer displayOrder) {
        Hotel hotel = getHotelEntity(hotelId);

        if (Boolean.TRUE.equals(isMainImage)) {
            hotel.getImages().forEach(image -> image.setIsMainImage(false));
        }

        HotelImage image = HotelImage.builder()
                .hotel(hotel)
                .title(title)
                .description(description)
                .isMainImage(isMainImage)
                .displayOrder(displayOrder)
                .build();

        hotelRepository.save(hotel);
        HotelImage savedImage = hotelImageRepository.save(image);

        String objectBaseName = hotelId + "-" + savedImage.getId();
        ImageUploadResult uploadResult = imageUploadService.uploadImage(file, objectBaseName);
        savedImage.setImageUrl(uploadResult.getPublicUrl());
        savedImage.setStoragePath(uploadResult.getObjectPath());
        HotelImage updatedImage = hotelImageRepository.save(savedImage);

        return HotelImageResponse.builder()
                .id(updatedImage.getId())
                .imageUrl(updatedImage.getImageUrl())
                .title(updatedImage.getTitle())
                .description(updatedImage.getDescription())
                .isMainImage(updatedImage.getIsMainImage())
                .displayOrder(updatedImage.getDisplayOrder())
                .createdAt(updatedImage.getCreatedAt())
                .build();
    }

    @Override
    public HotelImageResponse updateHotelImage(Long hotelId, Long imageId, MultipartFile file, String title,
                                               String description, Boolean isMainImage, Integer displayOrder) {
        Hotel hotel = getHotelEntity(hotelId);
        HotelImage image = hotel.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));

        if (Boolean.TRUE.equals(isMainImage)) {
            hotel.getImages().forEach(img -> img.setIsMainImage(false));
        }

        if (file != null && !file.isEmpty()) {
            imageUploadService.deleteImage(image.getStoragePath());
            String objectBaseName = hotelId + "-" + image.getId();
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

        HotelImage savedImage = hotelImageRepository.save(image);
        return HotelImageResponse.builder()
                .id(savedImage.getId())
                .imageUrl(savedImage.getImageUrl())
                .title(savedImage.getTitle())
                .description(savedImage.getDescription())
                .isMainImage(savedImage.getIsMainImage())
                .displayOrder(savedImage.getDisplayOrder())
                .createdAt(savedImage.getCreatedAt())
                .build();
    }

    @Override
    public void deleteHotelImage(Long hotelId, Long imageId) {
        Hotel hotel = getHotelEntity(hotelId);
        HotelImage image = hotel.getImages().stream()
                .filter(img -> img.getId().equals(imageId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Image not found: " + imageId));

        String storageTarget = image.getStoragePath();
        if (storageTarget == null || storageTarget.isBlank()) {
            storageTarget = image.getImageUrl();
        }
        imageUploadService.deleteImage(storageTarget);
        hotel.getImages().remove(image);
        hotelRepository.save(hotel);
        hotelImageRepository.delete(image);
    }

    private Hotel getHotelEntity(Long id) {
        return hotelRepository.findById(id)
                .filter(hotel -> Boolean.FALSE.equals(hotel.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + id));
    }
}
