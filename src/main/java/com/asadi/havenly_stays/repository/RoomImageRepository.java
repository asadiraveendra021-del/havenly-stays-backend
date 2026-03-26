package com.asadi.havenly_stays.repository;

import com.asadi.havenly_stays.entity.RoomImage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomImageRepository extends JpaRepository<RoomImage, Long> {
    List<RoomImage> findByRoomTypeId(Long roomTypeId);
    Optional<RoomImage> findByIdAndRoomTypeId(Long id, Long roomTypeId);
}
