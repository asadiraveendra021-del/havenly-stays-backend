package com.asadi.havenly_stays.repository;

import com.asadi.havenly_stays.entity.RoomAvailability;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {
    List<RoomAvailability> findByRoomTypeId(Long roomTypeId);
    List<RoomAvailability> findByRoomTypeIdOrderByDateAsc(Long roomTypeId);
    List<RoomAvailability> findByRoomTypeIdAndDateBetween(Long roomTypeId, LocalDate startDate, LocalDate endDate);
    Optional<RoomAvailability> findByRoomTypeIdAndDate(Long roomTypeId, LocalDate date);
    List<RoomAvailability> findByRoomTypeIdInOrderByDateAsc(List<Long> roomTypeIds);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from RoomAvailability r " +
            "where r.roomTypeId = :roomTypeId " +
            "and r.date >= :checkInDate " +
            "and r.date < :checkOutDate")
    List<RoomAvailability> findByRoomTypeIdAndDateRangeForUpdate(@Param("roomTypeId") Long roomTypeId,
                                                                 @Param("checkInDate") LocalDate checkInDate,
                                                                 @Param("checkOutDate") LocalDate checkOutDate);

    @Query("select r from RoomAvailability r " +
            "where r.roomTypeId in :roomTypeIds " +
            "and r.date >= :checkInDate " +
            "and r.date < :checkOutDate")
    List<RoomAvailability> findByRoomTypeIdsAndDateRange(@Param("roomTypeIds") List<Long> roomTypeIds,
                                                         @Param("checkInDate") LocalDate checkInDate,
                                                         @Param("checkOutDate") LocalDate checkOutDate);

    @Query("select min(r.price) from RoomAvailability r where r.roomTypeId = :roomTypeId")
    Double findMinPriceByRoomTypeId(@Param("roomTypeId") Long roomTypeId);
}
