package com.asadi.havenly_stays.service;

import com.asadi.havenly_stays.dto.MealPlanCreateRequest;
import com.asadi.havenly_stays.entity.MealPlan;
import java.util.List;

public interface MealPlanService {
    MealPlan createMealPlan(Long roomTypeId, MealPlanCreateRequest request);
    List<MealPlan> getMealPlans(Long roomTypeId);
    List<MealPlan> getActiveMealPlans(Long roomTypeId);
    MealPlan getMealPlan(Long mealPlanId);
}
