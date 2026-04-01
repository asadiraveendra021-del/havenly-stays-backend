package com.asadi.havenly_stays.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomAvailabilityResult {
    private Long roomTypeId;
    private String roomName;
    private Integer maxGuests;
    private Double totalPrice;
    private boolean available;
    private Integer availableRooms;
    private Double basePrice;
    private List<RoomAvailabilityMealPlanPrice> mealPlans;
}
