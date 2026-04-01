package com.asadi.havenly_stays.dto;

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
import org.springframework.format.annotation.DateTimeFormat;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchAvailabilityRequest {

    private String location;

    @NotNull(message = "checkInDate is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkInDate;

    @NotNull(message = "checkOutDate is required")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate checkOutDate;

    @NotNull(message = "guests is required")
    @Min(value = 1, message = "guests must be greater than 0")
    private Integer guests;

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
