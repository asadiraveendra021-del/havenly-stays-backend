package com.asadi.havenly_stays.controller;

import com.asadi.havenly_stays.dto.MealPlanCreateRequest;
import com.asadi.havenly_stays.dto.MealPlanResponse;
import com.asadi.havenly_stays.entity.MealPlan;
import com.asadi.havenly_stays.service.MealPlanService;
import com.asadi.havenly_stays.util.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/room-types/{roomTypeId}/meal-plans")
@Tag(name = "Admin Meal Plans", description = "Admin operations for meal plans")
public class AdminMealPlanController {

    private final MealPlanService mealPlanService;

    public AdminMealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    @PostMapping
    @Operation(summary = "Create a meal plan for a room type")
    public ResponseEntity<ApiResponse<MealPlanResponse>> createMealPlan(@PathVariable Long roomTypeId,
                                                                        @Valid @RequestBody MealPlanCreateRequest request) {
        MealPlan mealPlan = mealPlanService.createMealPlan(roomTypeId, request);
        MealPlanResponse response = toResponse(mealPlan);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<MealPlanResponse>builder()
                        .success(true)
                        .message("Meal plan created successfully")
                        .data(response)
                        .build());
    }

    @GetMapping
    @Operation(summary = "Get meal plans for a room type")
    public ResponseEntity<ApiResponse<List<MealPlanResponse>>> getMealPlans(@PathVariable Long roomTypeId) {
        List<MealPlanResponse> response = mealPlanService.getMealPlans(roomTypeId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.<List<MealPlanResponse>>builder()
                .success(true)
                .message("Meal plans fetched successfully")
                .data(response)
                .build());
    }

    private MealPlanResponse toResponse(MealPlan mealPlan) {
        return MealPlanResponse.builder()
                .id(mealPlan.getId())
                .name(mealPlan.getName())
                .pricePerDay(mealPlan.getPricePerDay())
                .build();
    }
}
