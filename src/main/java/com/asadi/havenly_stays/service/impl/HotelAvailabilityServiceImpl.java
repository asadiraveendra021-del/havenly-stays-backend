package com.asadi.havenly_stays.service.impl;

import com.asadi.havenly_stays.dto.HotelAvailabilityResponse;
import com.asadi.havenly_stays.dto.MealPlanResponse;
import com.asadi.havenly_stays.dto.RoomAvailabilityDateResponse;
import com.asadi.havenly_stays.dto.RoomAvailabilityDetails;
import com.asadi.havenly_stays.dto.RoomFacilityResponse;
import com.asadi.havenly_stays.entity.Hotel;
import com.asadi.havenly_stays.entity.MealPlan;
import com.asadi.havenly_stays.entity.RoomAvailability;
import com.asadi.havenly_stays.entity.RoomFacility;
import com.asadi.havenly_stays.entity.RoomType;
import com.asadi.havenly_stays.entity.RoomTypeFacility;
import com.asadi.havenly_stays.exception.ResourceNotFoundException;
import com.asadi.havenly_stays.repository.HotelRepository;
import com.asadi.havenly_stays.repository.MealPlanRepository;
import com.asadi.havenly_stays.repository.RoomAvailabilityRepository;
import com.asadi.havenly_stays.repository.RoomFacilityRepository;
import com.asadi.havenly_stays.repository.RoomTypeFacilityRepository;
import com.asadi.havenly_stays.repository.RoomTypeRepository;
import com.asadi.havenly_stays.service.HotelAvailabilityService;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class HotelAvailabilityServiceImpl implements HotelAvailabilityService {

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final MealPlanRepository mealPlanRepository;
    private final RoomTypeFacilityRepository roomTypeFacilityRepository;
    private final RoomFacilityRepository roomFacilityRepository;

    public HotelAvailabilityServiceImpl(HotelRepository hotelRepository,
                                        RoomTypeRepository roomTypeRepository,
                                        RoomAvailabilityRepository roomAvailabilityRepository,
                                        MealPlanRepository mealPlanRepository,
                                        RoomTypeFacilityRepository roomTypeFacilityRepository,
                                        RoomFacilityRepository roomFacilityRepository) {
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.roomTypeFacilityRepository = roomTypeFacilityRepository;
        this.roomFacilityRepository = roomFacilityRepository;
    }

    @Override
    public HotelAvailabilityResponse getAvailabilityDetails(Long hotelId) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .filter(entity -> Boolean.FALSE.equals(entity.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + hotelId));

        List<RoomType> roomTypes = roomTypeRepository.findByHotelId(hotelId);
        if (roomTypes.isEmpty()) {
            return HotelAvailabilityResponse.builder()
                    .hotelId(hotel.getId())
                    .hotelName(hotel.getName())
                    .rooms(Collections.emptyList())
                    .build();
        }

        List<Long> roomTypeIds = roomTypes.stream()
                .map(RoomType::getId)
                .collect(Collectors.toList());

        Map<Long, List<RoomAvailabilityDateResponse>> availabilityByRoomType = roomAvailabilityRepository
                .findByRoomTypeIdInOrderByDateAsc(roomTypeIds).stream()
                .collect(Collectors.groupingBy(RoomAvailability::getRoomTypeId,
                        Collectors.mapping(this::toAvailabilityResponse, Collectors.toList())));

        Map<Long, List<MealPlanResponse>> mealPlansByRoomType = mealPlanRepository
                .findByRoomTypeIdInAndIsActiveTrue(roomTypeIds).stream()
                .collect(Collectors.groupingBy(MealPlan::getRoomTypeId,
                        Collectors.mapping(this::toMealPlanResponse, Collectors.toList())));

        Map<Long, List<RoomFacilityResponse>> facilitiesByRoomType = buildFacilitiesByRoomType(roomTypeIds);

        List<RoomAvailabilityDetails> rooms = new ArrayList<>();
        for (RoomType roomType : roomTypes) {
            RoomAvailabilityDetails details = RoomAvailabilityDetails.builder()
                    .roomTypeId(roomType.getId())
                    .roomName(roomType.getName())
                    .basePrice(roomType.getBasePrice())
                    .facilities(facilitiesByRoomType.getOrDefault(roomType.getId(), Collections.emptyList()))
                    .availability(availabilityByRoomType.getOrDefault(roomType.getId(), Collections.emptyList()))
                    .mealPlans(mealPlansByRoomType.getOrDefault(roomType.getId(), Collections.emptyList()))
                    .build();
            rooms.add(details);
        }

        return HotelAvailabilityResponse.builder()
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .rooms(rooms)
                .build();
    }

    private Map<Long, List<RoomFacilityResponse>> buildFacilitiesByRoomType(List<Long> roomTypeIds) {
        List<RoomTypeFacility> mappings = roomTypeFacilityRepository.findByRoomTypeIdIn(roomTypeIds);
        if (mappings.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<Long, List<Long>> facilityIdsByRoomType = mappings.stream()
                .collect(Collectors.groupingBy(RoomTypeFacility::getRoomTypeId,
                        Collectors.mapping(RoomTypeFacility::getFacilityId, Collectors.toList())));

        List<Long> allFacilityIds = mappings.stream()
                .map(RoomTypeFacility::getFacilityId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, RoomFacility> facilityMap = roomFacilityRepository.findAllById(allFacilityIds).stream()
                .collect(Collectors.toMap(RoomFacility::getId, facility -> facility));

        Map<Long, List<RoomFacilityResponse>> result = new HashMap<>();
        for (Map.Entry<Long, List<Long>> entry : facilityIdsByRoomType.entrySet()) {
            List<RoomFacilityResponse> facilities = entry.getValue().stream()
                    .map(facilityMap::get)
                    .filter(entity -> entity != null)
                    .map(this::toFacilityResponse)
                    .collect(Collectors.toList());
            result.put(entry.getKey(), facilities);
        }
        return result;
    }

    private RoomAvailabilityDateResponse toAvailabilityResponse(RoomAvailability availability) {
        return RoomAvailabilityDateResponse.builder()
                .date(availability.getDate())
                .price(availability.getPrice())
                .availableRooms(availability.getAvailableRooms())
                .build();
    }

    private MealPlanResponse toMealPlanResponse(MealPlan mealPlan) {
        return MealPlanResponse.builder()
                .id(mealPlan.getId())
                .name(mealPlan.getName())
                .pricePerDay(mealPlan.getPricePerDay())
                .build();
    }

    private RoomFacilityResponse toFacilityResponse(RoomFacility facility) {
        return RoomFacilityResponse.builder()
                .id(facility.getId())
                .name(facility.getName())
                .iconCode(facility.getIconCode())
                .category(facility.getCategory())
                .build();
    }
}
