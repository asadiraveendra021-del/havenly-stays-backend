package com.asadi.havenly_stays.service.impl;

import com.asadi.havenly_stays.dto.RoomAvailabilityResult;
import com.asadi.havenly_stays.dto.RoomAvailabilityMealPlanPrice;
import com.asadi.havenly_stays.dto.SearchAvailabilityRequest;
import com.asadi.havenly_stays.dto.SearchAvailabilityResponse;
import com.asadi.havenly_stays.entity.Hotel;
import com.asadi.havenly_stays.entity.MealPlan;
import com.asadi.havenly_stays.entity.RoomAvailability;
import com.asadi.havenly_stays.entity.RoomType;
import com.asadi.havenly_stays.repository.HotelRepository;
import com.asadi.havenly_stays.repository.MealPlanRepository;
import com.asadi.havenly_stays.repository.RoomAvailabilityRepository;
import com.asadi.havenly_stays.repository.RoomTypeRepository;
import com.asadi.havenly_stays.service.SearchService;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class SearchServiceImpl implements SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchServiceImpl.class);

    private final HotelRepository hotelRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final MealPlanRepository mealPlanRepository;

    public SearchServiceImpl(HotelRepository hotelRepository,
                             RoomTypeRepository roomTypeRepository,
                             RoomAvailabilityRepository roomAvailabilityRepository,
                             MealPlanRepository mealPlanRepository) {
        this.hotelRepository = hotelRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.mealPlanRepository = mealPlanRepository;
    }

    @Override
    public List<SearchAvailabilityResponse> searchAvailability(SearchAvailabilityRequest request) {
        logger.info("Searching availability for location={}, checkInDate={}, checkOutDate={}, guests={}",
                request.getLocation(), request.getCheckInDate(), request.getCheckOutDate(), request.getGuests());

        List<Hotel> hotels = filterHotels(request.getLocation());
        if (hotels.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> hotelIds = hotels.stream()
                .map(Hotel::getId)
                .collect(Collectors.toList());

        List<RoomType> roomTypes = roomTypeRepository.findByHotelIdInAndMaxGuestsGreaterThanEqual(
                hotelIds, request.getGuests());
        if (roomTypes.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> roomTypeIds = roomTypes.stream()
                .map(RoomType::getId)
                .collect(Collectors.toList());

        List<RoomAvailability> availabilities = roomAvailabilityRepository.findByRoomTypeIdsAndDateRange(
                roomTypeIds, request.getCheckInDate(), request.getCheckOutDate());

        Map<Long, List<RoomAvailability>> availabilityByRoomType = availabilities.stream()
                .collect(Collectors.groupingBy(RoomAvailability::getRoomTypeId));

        int totalNights = (int) ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        Map<Long, List<RoomAvailabilityResult>> roomsByHotel = new HashMap<>();

        List<Long> roomTypeIdsForMealPlans = roomTypes.stream()
                .map(RoomType::getId)
                .collect(Collectors.toList());
        Map<Long, List<MealPlan>> mealPlansByRoomType = mealPlanRepository.findByRoomTypeIdInAndIsActiveTrue(
                        roomTypeIdsForMealPlans).stream()
                .collect(Collectors.groupingBy(MealPlan::getRoomTypeId));

        for (RoomType roomType : roomTypes) {
            List<RoomAvailability> roomAvailability = availabilityByRoomType.get(roomType.getId());
            List<MealPlan> mealPlans = mealPlansByRoomType.getOrDefault(roomType.getId(), List.of());
            RoomAvailabilityResult result = calculateRoomAvailability(roomType, roomAvailability, totalNights, mealPlans);
            if (result != null) {
                roomsByHotel.computeIfAbsent(roomType.getHotelId(), key -> new ArrayList<>()).add(result);
            }
        }

        List<SearchAvailabilityResponse> responses = new ArrayList<>();
        for (Hotel hotel : hotels) {
            List<RoomAvailabilityResult> results = roomsByHotel.get(hotel.getId());
            if (results != null && !results.isEmpty()) {
                responses.add(buildResponse(hotel, results));
            }
        }

        return responses;
    }

    private List<Hotel> filterHotels(String location) {
        if (location == null || location.isBlank()) {
            return hotelRepository.findByIsDeletedFalse();
        }
        return hotelRepository.searchByLocation(location.trim());
    }

    private RoomAvailabilityResult calculateRoomAvailability(RoomType roomType,
                                                             List<RoomAvailability> availabilities,
                                                             int totalNights,
                                                             List<MealPlan> mealPlans) {
        if (availabilities == null || availabilities.isEmpty()) {
            return null;
        }
        long distinctDates = availabilities.stream()
                .map(RoomAvailability::getDate)
                .distinct()
                .count();
        if (distinctDates != totalNights) {
            return null;
        }
        boolean anyUnavailable = availabilities.stream()
                .anyMatch(availability -> availability.getAvailableRooms() == null || availability.getAvailableRooms() <= 0);
        if (anyUnavailable) {
            return null;
        }

        Double totalPrice = availabilities.stream()
                .map(RoomAvailability::getPrice)
                .filter(Objects::nonNull)
                .mapToDouble(Double::doubleValue)
                .sum();

        Integer minAvailableRooms = availabilities.stream()
                .map(RoomAvailability::getAvailableRooms)
                .filter(Objects::nonNull)
                .min(Integer::compareTo)
                .orElse(0);

        List<RoomAvailabilityMealPlanPrice> mealPlanPrices = mealPlans.stream()
                .map(plan -> RoomAvailabilityMealPlanPrice.builder()
                        .mealPlanId(plan.getId())
                        .name(plan.getName())
                        .pricePerDay(plan.getPricePerDay())
                        .totalPrice(totalPrice + (plan.getPricePerDay() * totalNights))
                        .build())
                .collect(Collectors.toList());

        return RoomAvailabilityResult.builder()
                .roomTypeId(roomType.getId())
                .roomName(roomType.getName())
                .maxGuests(roomType.getMaxGuests())
                .totalPrice(totalPrice)
                .available(true)
                .availableRooms(minAvailableRooms)
                .basePrice(totalPrice)
                .mealPlans(mealPlanPrices)
                .build();
    }

    private SearchAvailabilityResponse buildResponse(Hotel hotel, List<RoomAvailabilityResult> rooms) {
        return SearchAvailabilityResponse.builder()
                .hotelId(hotel.getId())
                .hotelName(hotel.getName())
                .city(hotel.getCity())
                .starRating(hotel.getStarRating())
                .rooms(rooms)
                .build();
    }
}
