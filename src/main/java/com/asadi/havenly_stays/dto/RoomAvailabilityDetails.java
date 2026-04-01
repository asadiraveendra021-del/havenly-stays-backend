package com.asadi.havenly_stays.dto;

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
public class RoomAvailabilityDetails {
    private Long roomTypeId;
    private String roomName;
    private Double basePrice;
    private List<RoomFacilityResponse> facilities;
    private List<RoomAvailabilityDateResponse> availability;
    private List<MealPlanResponse> mealPlans;
}
