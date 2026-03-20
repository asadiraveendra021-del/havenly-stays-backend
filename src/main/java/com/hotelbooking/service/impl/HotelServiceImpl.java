package com.hotelbooking.service.impl;

import com.hotelbooking.dto.HotelCreateRequest;
import com.hotelbooking.dto.HotelImageResponse;
import com.hotelbooking.dto.HotelPetPolicyRequest;
import com.hotelbooking.dto.HotelPetPolicyResponse;
import com.hotelbooking.dto.HotelResponse;
import com.hotelbooking.dto.HotelUpdateRequest;
import com.hotelbooking.entity.Facility;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.HotelFacility;
import com.hotelbooking.entity.HotelImage;
import com.hotelbooking.entity.HotelPetPolicy;
import com.hotelbooking.exception.ResourceNotFoundException;
import com.hotelbooking.mapper.HotelMapper;
import com.hotelbooking.repository.FacilityRepository;
import com.hotelbooking.repository.HotelRepository;
import com.hotelbooking.service.HotelService;
import com.hotelbooking.service.ImageUploadService;
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
    private final ImageUploadService imageUploadService;
    private final HotelMapper hotelMapper;

    public HotelServiceImpl(HotelRepository hotelRepository,
                            FacilityRepository facilityRepository,
                            ImageUploadService imageUploadService,
                            HotelMapper hotelMapper) {
        this.hotelRepository = hotelRepository;
        this.facilityRepository = facilityRepository;
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
        String imageUrl = imageUploadService.uploadImage(file);

        if (Boolean.TRUE.equals(isMainImage)) {
            hotel.getImages().forEach(image -> image.setIsMainImage(false));
        }

        HotelImage image = HotelImage.builder()
                .hotel(hotel)
                .imageUrl(imageUrl)
                .title(title)
                .description(description)
                .isMainImage(isMainImage)
                .displayOrder(displayOrder)
                .build();

        hotel.getImages().add(image);
        Hotel saved = hotelRepository.save(hotel);

        return saved.getImages().stream()
                .filter(img -> img.getImageUrl().equals(imageUrl))
                .findFirst()
                .map(img -> HotelImageResponse.builder()
                        .id(img.getId())
                        .imageUrl(img.getImageUrl())
                        .title(img.getTitle())
                        .description(img.getDescription())
                        .isMainImage(img.getIsMainImage())
                        .displayOrder(img.getDisplayOrder())
                        .createdAt(img.getCreatedAt())
                        .build())
                .orElseThrow(() -> new ResourceNotFoundException("Image could not be saved"));
    }

    private Hotel getHotelEntity(Long id) {
        return hotelRepository.findById(id)
                .filter(hotel -> Boolean.FALSE.equals(hotel.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + id));
    }
}
