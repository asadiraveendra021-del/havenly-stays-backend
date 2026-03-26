package com.asadi.havenly_stays.repository;

import com.asadi.havenly_stays.entity.RoomType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    List<RoomType> findByHotelId(Long hotelId);
    Optional<RoomType> findByIdAndHotelId(Long id, Long hotelId);
}
