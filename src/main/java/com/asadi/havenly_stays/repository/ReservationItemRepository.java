package com.asadi.havenly_stays.repository;

import com.asadi.havenly_stays.entity.ReservationItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationItemRepository extends JpaRepository<ReservationItem, Long> {
    List<ReservationItem> findByReservationId(Long reservationId);
}
