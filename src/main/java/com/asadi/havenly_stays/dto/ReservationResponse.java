package com.asadi.havenly_stays.dto;

import com.asadi.havenly_stays.entity.ReservationStatus;
import java.time.LocalDateTime;
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
public class ReservationResponse {
    private Long reservationId;
    private Long userId;
    private Long hotelId;
    private List<ReservationItemResponse> items;
    private Double totalPrice;
    private ReservationStatus status;
    private LocalDateTime holdExpiryTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
