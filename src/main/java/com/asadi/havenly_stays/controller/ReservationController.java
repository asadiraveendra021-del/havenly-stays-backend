package com.asadi.havenly_stays.controller;

import com.asadi.havenly_stays.dto.ReservationItemResponse;
import com.asadi.havenly_stays.dto.ReservationRequest;
import com.asadi.havenly_stays.dto.ReservationResponse;
import com.asadi.havenly_stays.entity.MealPlan;
import com.asadi.havenly_stays.entity.MealPlanName;
import com.asadi.havenly_stays.entity.Reservation;
import com.asadi.havenly_stays.entity.ReservationItem;
import com.asadi.havenly_stays.service.MealPlanService;
import com.asadi.havenly_stays.service.ReservationService;
import com.asadi.havenly_stays.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reservations")
@Tag(name = "Reservations", description = "Reservation lifecycle APIs")
@Validated
public class ReservationController {

    private final ReservationService reservationService;
    private final MealPlanService mealPlanService;

    public ReservationController(ReservationService reservationService,
                                 MealPlanService mealPlanService) {
        this.reservationService = reservationService;
        this.mealPlanService = mealPlanService;
    }

    @PostMapping("/pre-book")
    @Operation(summary = "Pre-book reservation (hold)")
    public ResponseEntity<ApiResponse<ReservationResponse>> preBook(@Valid @RequestBody ReservationRequest request) {
        Reservation reservation = reservationService.preBook(request);
        return ResponseEntity.ok(ApiResponse.<ReservationResponse>builder()
                .success(true)
                .message("Reservation pre-booked successfully")
                .data(toResponse(reservation))
                .build());
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm reservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> confirm(@PathVariable Long id) {
        Reservation reservation = reservationService.confirmReservation(id);
        return ResponseEntity.ok(ApiResponse.<ReservationResponse>builder()
                .success(true)
                .message("Reservation confirmed successfully")
                .data(toResponse(reservation))
                .build());
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel reservation")
    public ResponseEntity<ApiResponse<ReservationResponse>> cancel(@PathVariable Long id) {
        Reservation reservation = reservationService.cancelReservation(id);
        return ResponseEntity.ok(ApiResponse.<ReservationResponse>builder()
                .success(true)
                .message("Reservation cancelled successfully")
                .data(toResponse(reservation))
                .build());
    }

    private ReservationResponse toResponse(Reservation reservation) {
        List<ReservationItem> items = reservationService.getReservationItems(reservation.getId());
        List<ReservationItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .toList();

        return ReservationResponse.builder()
                .reservationId(reservation.getId())
                .userId(reservation.getUserId())
                .hotelId(reservation.getHotelId())
                .items(itemResponses)
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .holdExpiryTime(reservation.getHoldExpiryTime())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();
    }

    private ReservationItemResponse toItemResponse(ReservationItem item) {
        ReservationItemResponse response = ReservationItemResponse.builder()
                .roomTypeId(item.getRoomTypeId())
                .roomsBooked(item.getRoomsBooked())
                .checkInDate(item.getCheckInDate())
                .checkOutDate(item.getCheckOutDate())
                .mealPlanId(item.getMealPlanId())
                .roomPrice(item.getRoomPrice())
                .mealPrice(item.getMealPrice())
                .totalPrice(item.getTotalPrice())
                .build();

        if (item.getMealPlanId() != null) {
            MealPlan mealPlan = mealPlanService.getMealPlan(item.getMealPlanId());
            response.setMealPlanName(mealPlan.getName().name());
            response.setMealPlanPricePerDay(mealPlan.getPricePerDay());
        } else {
            response.setMealPlanName(MealPlanName.ROOM_ONLY.name());
            response.setMealPlanPricePerDay(0.0);
        }

        return response;
    }
}
