package com.asadi.havenly_stays.repository;

import com.asadi.havenly_stays.entity.MealPlan;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {
    List<MealPlan> findByRoomTypeId(Long roomTypeId);
    List<MealPlan> findByRoomTypeIdAndIsActiveTrue(Long roomTypeId);
    List<MealPlan> findByRoomTypeIdInAndIsActiveTrue(List<Long> roomTypeIds);
    Optional<MealPlan> findByIdAndRoomTypeId(Long id, Long roomTypeId);
}
