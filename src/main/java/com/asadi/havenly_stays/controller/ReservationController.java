package com.asadi.havenly_stays.controller;

import com.asadi.havenly_stays.dto.ReservationPreBookRequest;
import com.asadi.havenly_stays.dto.ReservationResponse;
import com.asadi.havenly_stays.entity.MealPlan;
import com.asadi.havenly_stays.entity.Reservation;
import com.asadi.havenly_stays.service.MealPlanService;
import com.asadi.havenly_stays.service.ReservationService;
import com.asadi.havenly_stays.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
    public ResponseEntity<ApiResponse<ReservationResponse>> preBook(@Valid @RequestBody ReservationPreBookRequest request) {
        Reservation reservation = reservationService.preBook(
                request.getUserId(),
                request.getRoomTypeId(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getRoomsRequired(),
                request.getMealPlanId());
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
        ReservationResponse response = ReservationResponse.builder()
                .id(reservation.getId())
                .userId(reservation.getUserId())
                .hotelId(reservation.getHotelId())
                .roomTypeId(reservation.getRoomTypeId())
                .checkInDate(reservation.getCheckInDate())
                .checkOutDate(reservation.getCheckOutDate())
                .roomsBooked(reservation.getRoomsBooked())
                .totalPrice(reservation.getTotalPrice())
                .status(reservation.getStatus())
                .holdExpiryTime(reservation.getHoldExpiryTime())
                .createdAt(reservation.getCreatedAt())
                .updatedAt(reservation.getUpdatedAt())
                .build();

        if (reservation.getMealPlanId() != null) {
            MealPlan mealPlan = mealPlanService.getMealPlan(reservation.getMealPlanId());
            response.setMealPlanName(mealPlan.getName().name());
            response.setMealPlanPricePerDay(mealPlan.getPricePerDay());
        }

        return response;
    }
}
