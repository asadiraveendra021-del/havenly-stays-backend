package com.asadi.havenly_stays.dto;

import com.asadi.havenly_stays.entity.MealPlanName;
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
public class RoomAvailabilityMealPlanPrice {
    private Long mealPlanId;
    private MealPlanName name;
    private Double pricePerDay;
    private Double totalPrice;
}
