package com.asadi.havenly_stays.dto;

import com.asadi.havenly_stays.entity.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationResponse {
    private Long id;
    private Long userId;
    private Long hotelId;
    private Long roomTypeId;
    private String mealPlanName;
    private Double mealPlanPricePerDay;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    private Integer roomsBooked;
    private Double totalPrice;
    private ReservationStatus status;
    private LocalDateTime holdExpiryTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
