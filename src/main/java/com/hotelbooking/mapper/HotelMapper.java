package com.hotelbooking.mapper;

import com.hotelbooking.dto.HotelCreateRequest;
import com.hotelbooking.dto.HotelImageResponse;
import com.hotelbooking.dto.HotelPetPolicyResponse;
import com.hotelbooking.dto.HotelResponse;
import com.hotelbooking.dto.HotelUpdateRequest;
import com.hotelbooking.entity.Hotel;
import com.hotelbooking.entity.HotelFacility;
import com.hotelbooking.entity.HotelImage;
import com.hotelbooking.entity.HotelPetPolicy;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class HotelMapper {

    private final FacilityMapper facilityMapper;

    public HotelMapper(FacilityMapper facilityMapper) {
        this.facilityMapper = facilityMapper;
    }

    public Hotel toEntity(HotelCreateRequest request) {
        if (request == null) {
            return null;
        }
        return Hotel.builder()
                .name(request.getName())
                .description(request.getDescription())
                .shortDescription(request.getShortDescription())
                .starRating(request.getStarRating())
                .address(request.getAddress())
                .city(request.getCity())
                .state(request.getState())
                .country(request.getCountry())
                .postalCode(request.getPostalCode())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .phone(request.getPhone())
                .email(request.getEmail())
                .website(request.getWebsite())
                .checkInTime(request.getCheckInTime())
                .checkOutTime(request.getCheckOutTime())
                .isActive(request.getIsActive())
                .isDeleted(false)
                .avgRating(0.0)
                .totalReviews(0)
                .build();
    }

    public void applyUpdate(Hotel hotel, HotelUpdateRequest request) {
        if (request.getName() != null) {
            hotel.setName(request.getName());
        }
        if (request.getDescription() != null) {
            hotel.setDescription(request.getDescription());
        }
        if (request.getShortDescription() != null) {
            hotel.setShortDescription(request.getShortDescription());
        }
        if (request.getStarRating() != null) {
            hotel.setStarRating(request.getStarRating());
        }
        if (request.getAddress() != null) {
            hotel.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            hotel.setCity(request.getCity());
        }
        if (request.getState() != null) {
            hotel.setState(request.getState());
        }
        if (request.getCountry() != null) {
            hotel.setCountry(request.getCountry());
        }
        if (request.getPostalCode() != null) {
            hotel.setPostalCode(request.getPostalCode());
        }
        if (request.getLatitude() != null) {
            hotel.setLatitude(request.getLatitude());
        }
        if (request.getLongitude() != null) {
            hotel.setLongitude(request.getLongitude());
        }
        if (request.getPhone() != null) {
            hotel.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            hotel.setEmail(request.getEmail());
        }
        if (request.getWebsite() != null) {
            hotel.setWebsite(request.getWebsite());
        }
        if (request.getCheckInTime() != null) {
            hotel.setCheckInTime(request.getCheckInTime());
        }
        if (request.getCheckOutTime() != null) {
            hotel.setCheckOutTime(request.getCheckOutTime());
        }
        if (request.getIsActive() != null) {
            hotel.setIsActive(request.getIsActive());
        }
    }

    public HotelResponse toResponse(Hotel hotel) {
        if (hotel == null) {
            return null;
        }
        List<HotelImageResponse> imageResponses = hotel.getImages().stream()
                .map(this::toImageResponse)
                .collect(Collectors.toList());

        List<com.hotelbooking.dto.FacilityResponse> facilities = hotel.getHotelFacilities().stream()
                .map(HotelFacility::getFacility)
                .map(facilityMapper::toResponse)
                .collect(Collectors.toList());

        return HotelResponse.builder()
                .id(hotel.getId())
                .name(hotel.getName())
                .description(hotel.getDescription())
                .shortDescription(hotel.getShortDescription())
                .starRating(hotel.getStarRating())
                .avgRating(hotel.getAvgRating())
                .totalReviews(hotel.getTotalReviews())
                .address(hotel.getAddress())
                .city(hotel.getCity())
                .state(hotel.getState())
                .country(hotel.getCountry())
                .postalCode(hotel.getPostalCode())
                .latitude(hotel.getLatitude())
                .longitude(hotel.getLongitude())
                .phone(hotel.getPhone())
                .email(hotel.getEmail())
                .website(hotel.getWebsite())
                .checkInTime(hotel.getCheckInTime())
                .checkOutTime(hotel.getCheckOutTime())
                .isActive(hotel.getIsActive())
                .isDeleted(hotel.getIsDeleted())
                .createdAt(hotel.getCreatedAt())
                .updatedAt(hotel.getUpdatedAt())
                .images(imageResponses)
                .facilities(facilities)
                .petPolicy(toPetPolicyResponse(hotel.getPetPolicy()))
                .build();
    }

    private HotelImageResponse toImageResponse(HotelImage image) {
        return HotelImageResponse.builder()
                .id(image.getId())
                .imageUrl(image.getImageUrl())
                .title(image.getTitle())
                .description(image.getDescription())
                .isMainImage(image.getIsMainImage())
                .displayOrder(image.getDisplayOrder())
                .createdAt(image.getCreatedAt())
                .build();
    }

    private HotelPetPolicyResponse toPetPolicyResponse(HotelPetPolicy policy) {
        if (policy == null) {
            return null;
        }
        return HotelPetPolicyResponse.builder()
                .id(policy.getId())
                .petsAllowed(policy.getPetsAllowed())
                .petFeeType(policy.getPetFeeType())
                .petFee(policy.getPetFee())
                .maxPets(policy.getMaxPets())
                .description(policy.getDescription())
                .build();
    }
}
