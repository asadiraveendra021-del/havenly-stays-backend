package com.asadi.havenly_stays.service.impl;

import com.asadi.havenly_stays.dto.MealPlanCreateRequest;
import com.asadi.havenly_stays.entity.MealPlan;
import com.asadi.havenly_stays.entity.RoomType;
import com.asadi.havenly_stays.exception.ResourceNotFoundException;
import com.asadi.havenly_stays.repository.MealPlanRepository;
import com.asadi.havenly_stays.repository.RoomTypeRepository;
import com.asadi.havenly_stays.service.MealPlanService;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MealPlanServiceImpl implements MealPlanService {

    private final MealPlanRepository mealPlanRepository;
    private final RoomTypeRepository roomTypeRepository;

    public MealPlanServiceImpl(MealPlanRepository mealPlanRepository,
                               RoomTypeRepository roomTypeRepository) {
        this.mealPlanRepository = mealPlanRepository;
        this.roomTypeRepository = roomTypeRepository;
    }

    @Override
    public MealPlan createMealPlan(Long roomTypeId, MealPlanCreateRequest request) {
        RoomType roomType = roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found: " + roomTypeId));

        MealPlan mealPlan = MealPlan.builder()
                .roomTypeId(roomType.getId())
                .name(request.getName())
                .description(request.getDescription())
                .pricePerDay(request.getPricePerDay())
                .isActive(request.getIsActive())
                .build();

        return mealPlanRepository.save(mealPlan);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MealPlan> getMealPlans(Long roomTypeId) {
        return mealPlanRepository.findByRoomTypeId(roomTypeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MealPlan> getActiveMealPlans(Long roomTypeId) {
        return mealPlanRepository.findByRoomTypeIdAndIsActiveTrue(roomTypeId);
    }

    @Override
    @Transactional(readOnly = true)
    public MealPlan getMealPlan(Long mealPlanId) {
        return mealPlanRepository.findById(mealPlanId)
                .orElseThrow(() -> new ResourceNotFoundException("Meal plan not found: " + mealPlanId));
    }
}
