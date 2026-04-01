package com.asadi.havenly_stays.dto;

import com.asadi.havenly_stays.entity.MealPlanName;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
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
public class MealPlanCreateRequest {

    @NotNull(message = "name is required")
    private MealPlanName name;

    private String description;

    @NotNull(message = "pricePerDay is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "pricePerDay must be >= 0")
    private Double pricePerDay;

    private Boolean isActive;
}
