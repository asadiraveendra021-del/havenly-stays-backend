package com.asadi.havenly_stays.service.impl;

import com.asadi.havenly_stays.dto.RoomAvailabilityBulkRequest;
import com.asadi.havenly_stays.dto.RoomAvailabilityRangeRequest;
import com.asadi.havenly_stays.dto.RoomAvailabilityResponse;
import com.asadi.havenly_stays.entity.RoomAvailability;
import com.asadi.havenly_stays.entity.RoomType;
import com.asadi.havenly_stays.exception.ResourceNotFoundException;
import com.asadi.havenly_stays.repository.RoomAvailabilityRepository;
import com.asadi.havenly_stays.repository.RoomTypeRepository;
import com.asadi.havenly_stays.service.RoomAvailabilityService;
import jakarta.validation.ValidationException;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoomAvailabilityServiceImpl implements RoomAvailabilityService {

    private static final Logger logger = LoggerFactory.getLogger(RoomAvailabilityServiceImpl.class);

    private final RoomTypeRepository roomTypeRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;

    public RoomAvailabilityServiceImpl(RoomTypeRepository roomTypeRepository,
                                       RoomAvailabilityRepository roomAvailabilityRepository) {
        this.roomTypeRepository = roomTypeRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
    }

    @Override
    public List<RoomAvailabilityResponse> createOrUpdateAvailabilities(Long roomTypeId,
                                                                       RoomAvailabilityBulkRequest request) {
        logger.info("Creating/updating availabilities roomTypeId={}", roomTypeId);
        getRoomTypeEntity(roomTypeId);
        Map<LocalDate, RoomAvailability> toSaveMap = new LinkedHashMap<>();

        for (RoomAvailabilityRangeRequest range : request.getRanges()) {
            if (range.getStartDate().isAfter(range.getEndDate())) {
                throw new ValidationException("Start date must be before or equal to end date");
            }

            Map<LocalDate, RoomAvailability> existingMap = roomAvailabilityRepository
                    .findByRoomTypeIdAndDateBetween(roomTypeId, range.getStartDate(), range.getEndDate())
                    .stream()
                    .collect(Collectors.toMap(RoomAvailability::getDate, availability -> availability));

            LocalDate date = range.getStartDate();
            while (!date.isAfter(range.getEndDate())) {
                RoomAvailability availability = existingMap.get(date);
                if (availability == null) {
                    availability = RoomAvailability.builder()
                            .roomTypeId(roomTypeId)
                            .date(date)
                            .price(range.getPrice())
                            .availableRooms(range.getAvailableRooms())
                            .build();
                } else {
                    availability.setPrice(range.getPrice());
                    availability.setAvailableRooms(range.getAvailableRooms());
                }
                toSaveMap.put(date, availability);
                date = date.plusDays(1);
            }
        }

        List<RoomAvailability> saved = roomAvailabilityRepository.saveAll(toSaveMap.values());
        return saved.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomAvailabilityResponse> getAvailabilities(Long roomTypeId) {
        logger.info("Fetching availabilities roomTypeId={}", roomTypeId);
        getRoomTypeEntity(roomTypeId);
        return roomAvailabilityRepository.findByRoomTypeIdOrderByDateAsc(roomTypeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private RoomType getRoomTypeEntity(Long id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found: " + id));
    }

    private RoomAvailabilityResponse toResponse(RoomAvailability availability) {
        return RoomAvailabilityResponse.builder()
                .id(availability.getId())
                .roomTypeId(availability.getRoomTypeId())
                .date(availability.getDate())
                .price(availability.getPrice())
                .availableRooms(availability.getAvailableRooms())
                .build();
    }
}
