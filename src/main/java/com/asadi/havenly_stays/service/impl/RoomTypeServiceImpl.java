package com.asadi.havenly_stays.service.impl;

import com.asadi.havenly_stays.dto.RoomFacilityResponse;
import com.asadi.havenly_stays.dto.RoomImageResponse;
import com.asadi.havenly_stays.dto.RoomTypeCreateRequest;
import com.asadi.havenly_stays.dto.RoomTypeResponse;
import com.asadi.havenly_stays.dto.RoomTypeUpdateRequest;
import com.asadi.havenly_stays.entity.Hotel;
import com.asadi.havenly_stays.entity.RoomImage;
import com.asadi.havenly_stays.entity.RoomType;
import com.asadi.havenly_stays.entity.RoomTypeFacility;
import com.asadi.havenly_stays.exception.ResourceNotFoundException;
import com.asadi.havenly_stays.mapper.RoomFacilityMapper;
import com.asadi.havenly_stays.mapper.RoomTypeMapper;
import com.asadi.havenly_stays.repository.HotelRepository;
import com.asadi.havenly_stays.repository.RoomAvailabilityRepository;
import com.asadi.havenly_stays.repository.RoomFacilityRepository;
import com.asadi.havenly_stays.repository.RoomImageRepository;
import com.asadi.havenly_stays.repository.RoomTypeFacilityRepository;
import com.asadi.havenly_stays.repository.RoomTypeRepository;
import com.asadi.havenly_stays.service.RoomTypeService;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoomTypeServiceImpl implements RoomTypeService {

    private static final Logger logger = LoggerFactory.getLogger(RoomTypeServiceImpl.class);

    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;
    private final RoomImageRepository roomImageRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final RoomTypeFacilityRepository roomTypeFacilityRepository;
    private final RoomFacilityRepository roomFacilityRepository;
    private final RoomTypeMapper roomTypeMapper;
    private final RoomFacilityMapper roomFacilityMapper;

    public RoomTypeServiceImpl(RoomTypeRepository roomTypeRepository,
                               HotelRepository hotelRepository,
                               RoomImageRepository roomImageRepository,
                               RoomAvailabilityRepository roomAvailabilityRepository,
                               RoomTypeFacilityRepository roomTypeFacilityRepository,
                               RoomFacilityRepository roomFacilityRepository,
                               RoomTypeMapper roomTypeMapper,
                               RoomFacilityMapper roomFacilityMapper) {
        this.roomTypeRepository = roomTypeRepository;
        this.hotelRepository = hotelRepository;
        this.roomImageRepository = roomImageRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.roomTypeFacilityRepository = roomTypeFacilityRepository;
        this.roomFacilityRepository = roomFacilityRepository;
        this.roomTypeMapper = roomTypeMapper;
        this.roomFacilityMapper = roomFacilityMapper;
    }

    @Override
    public RoomTypeResponse createRoomType(Long hotelId, RoomTypeCreateRequest request) {
        logger.info("Creating room type for hotelId={}", hotelId);
        Hotel hotel = getHotelEntity(hotelId);
        RoomType roomType = roomTypeMapper.toEntity(request, hotel.getId());
        RoomType saved = roomTypeRepository.save(roomType);
        return buildRoomTypeResponse(saved);
    }

    @Override
    public RoomTypeResponse updateRoomType(Long roomTypeId, RoomTypeUpdateRequest request) {
        logger.info("Updating room type roomTypeId={}", roomTypeId);
        RoomType roomType = getRoomTypeEntity(roomTypeId);
        roomTypeMapper.applyUpdate(roomType, request);
        RoomType saved = roomTypeRepository.save(roomType);
        return buildRoomTypeResponse(saved);
    }

    @Override
    public void deleteRoomType(Long roomTypeId) {
        logger.info("Deleting room type roomTypeId={}", roomTypeId);
        RoomType roomType = getRoomTypeEntity(roomTypeId);
        roomTypeRepository.delete(roomType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomTypeResponse> getRoomTypesByHotel(Long hotelId) {
        logger.info("Fetching room types for hotelId={}", hotelId);
        getHotelEntity(hotelId);
        return roomTypeRepository.findByHotelId(hotelId).stream()
                .map(this::buildRoomTypeResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoomTypeResponse getRoomTypeById(Long roomTypeId) {
        logger.info("Fetching room type by id={}", roomTypeId);
        RoomType roomType = getRoomTypeEntity(roomTypeId);
        return buildRoomTypeResponse(roomType);
    }

    private RoomTypeResponse buildRoomTypeResponse(RoomType roomType) {
        List<RoomImageResponse> images = roomImageRepository.findByRoomTypeId(roomType.getId()).stream()
                .map(this::toImageResponse)
                .collect(Collectors.toList());

        List<RoomTypeFacility> mappings = roomTypeFacilityRepository.findByRoomTypeId(roomType.getId());
        List<Long> facilityIds = mappings.stream()
                .map(RoomTypeFacility::getFacilityId)
                .collect(Collectors.toList());

        List<RoomFacilityResponse> facilities = facilityIds.isEmpty()
                ? Collections.emptyList()
                : roomFacilityRepository.findAllById(facilityIds).stream()
                .map(roomFacilityMapper::toResponse)
                .collect(Collectors.toList());

        Double minPrice = roomAvailabilityRepository.findMinPriceByRoomTypeId(roomType.getId());
        return roomTypeMapper.toResponse(roomType, images, facilities, minPrice);
    }

    private RoomImageResponse toImageResponse(RoomImage image) {
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

    private Hotel getHotelEntity(Long id) {
        return hotelRepository.findById(id)
                .filter(hotel -> Boolean.FALSE.equals(hotel.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + id));
    }

    private RoomType getRoomTypeEntity(Long id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found: " + id));
    }
}
