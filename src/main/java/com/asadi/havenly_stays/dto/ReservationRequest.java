package com.asadi.havenly_stays.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;
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
public class ReservationRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "hotelId is required")
    private Long hotelId;

    @NotEmpty(message = "bookings must not be empty")
    @Valid
    private List<ReservationBookingItemRequest> bookings;
}
