package com.asadi.havenly_stays.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
public class ReservationBookingItemRequest {

    @NotNull(message = "roomTypeId is required")
    private Long roomTypeId;

    @NotNull(message = "roomsRequired is required")
    @Min(value = 1, message = "roomsRequired must be greater than 0")
    private Integer roomsRequired;

    @NotNull(message = "checkInDate is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkInDate;

    @NotNull(message = "checkOutDate is required")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate checkOutDate;

    private Long mealPlanId;

    @AssertTrue(message = "checkInDate must be before checkOutDate")
    public boolean isCheckInBeforeCheckOut() {
        if (checkInDate == null || checkOutDate == null) {
            return true;
        }
        return checkInDate.isBefore(checkOutDate);
    }

    @AssertTrue(message = "date range should not exceed 30 days")
    public boolean isDateRangeValid() {
        if (checkInDate == null || checkOutDate == null) {
            return true;
        }
        long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        return days <= 30;
    }
}
