package com.asadi.havenly_stays.service.impl;

import com.asadi.havenly_stays.dto.RoomFacilityResponse;
import com.asadi.havenly_stays.entity.RoomFacility;
import com.asadi.havenly_stays.entity.RoomType;
import com.asadi.havenly_stays.entity.RoomTypeFacility;
import com.asadi.havenly_stays.exception.ResourceNotFoundException;
import com.asadi.havenly_stays.mapper.RoomFacilityMapper;
import com.asadi.havenly_stays.repository.RoomFacilityRepository;
import com.asadi.havenly_stays.repository.RoomTypeFacilityRepository;
import com.asadi.havenly_stays.repository.RoomTypeRepository;
import com.asadi.havenly_stays.service.RoomFacilityService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RoomFacilityServiceImpl implements RoomFacilityService {

    private static final Logger logger = LoggerFactory.getLogger(RoomFacilityServiceImpl.class);

    private final RoomTypeRepository roomTypeRepository;
    private final RoomFacilityRepository roomFacilityRepository;
    private final RoomTypeFacilityRepository roomTypeFacilityRepository;
    private final RoomFacilityMapper roomFacilityMapper;

    public RoomFacilityServiceImpl(RoomTypeRepository roomTypeRepository,
                                   RoomFacilityRepository roomFacilityRepository,
                                   RoomTypeFacilityRepository roomTypeFacilityRepository,
                                   RoomFacilityMapper roomFacilityMapper) {
        this.roomTypeRepository = roomTypeRepository;
        this.roomFacilityRepository = roomFacilityRepository;
        this.roomTypeFacilityRepository = roomTypeFacilityRepository;
        this.roomFacilityMapper = roomFacilityMapper;
    }

    @Override
    public List<RoomFacilityResponse> setFacilities(Long roomTypeId, List<Long> facilityIds) {
        logger.info("Setting facilities for roomTypeId={}", roomTypeId);
        getRoomTypeEntity(roomTypeId);
        Map<Long, RoomFacility> facilityMap = roomFacilityRepository.findAllById(facilityIds).stream()
                .collect(Collectors.toMap(RoomFacility::getId, facility -> facility));

        List<Long> missing = facilityIds.stream()
                .filter(id -> !facilityMap.containsKey(id))
                .collect(Collectors.toList());
        if (!missing.isEmpty()) {
            throw new ResourceNotFoundException("Room facilities not found: " + missing);
        }

        roomTypeFacilityRepository.deleteByRoomTypeId(roomTypeId);
        List<RoomTypeFacility> mappings = facilityIds.stream()
                .map(facilityId -> RoomTypeFacility.builder()
                        .roomTypeId(roomTypeId)
                        .facilityId(facilityId)
                        .build())
                .collect(Collectors.toList());
        roomTypeFacilityRepository.saveAll(mappings);

        return facilityMap.values().stream()
                .map(roomFacilityMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomFacilityResponse> getFacilities(Long roomTypeId) {
        logger.info("Fetching facilities for roomTypeId={}", roomTypeId);
        getRoomTypeEntity(roomTypeId);
        List<Long> facilityIds = roomTypeFacilityRepository.findByRoomTypeId(roomTypeId).stream()
                .map(RoomTypeFacility::getFacilityId)
                .collect(Collectors.toList());
        if (facilityIds.isEmpty()) {
            return List.of();
        }
        return roomFacilityRepository.findAllById(facilityIds).stream()
                .map(roomFacilityMapper::toResponse)
                .collect(Collectors.toList());
    }

    private RoomType getRoomTypeEntity(Long id) {
        return roomTypeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found: " + id));
    }
}
