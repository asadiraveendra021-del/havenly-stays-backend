package com.asadi.havenly_stays.repository;

import com.asadi.havenly_stays.entity.RoomTypeFacility;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTypeFacilityRepository extends JpaRepository<RoomTypeFacility, Long> {
    List<RoomTypeFacility> findByRoomTypeId(Long roomTypeId);
    void deleteByRoomTypeId(Long roomTypeId);
}
