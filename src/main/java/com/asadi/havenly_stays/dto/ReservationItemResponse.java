package com.asadi.havenly_stays.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDate;
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
public class ReservationItemResponse {
    private Long roomTypeId;
    private Integer roomsBooked;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    private Long mealPlanId;
    private String mealPlanName;
    private Double mealPlanPricePerDay;
    private Double roomPrice;
    private Double mealPrice;
    private Double totalPrice;
}
