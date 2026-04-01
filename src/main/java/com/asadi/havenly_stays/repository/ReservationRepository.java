package com.asadi.havenly_stays.repository;

import com.asadi.havenly_stays.entity.Reservation;
import com.asadi.havenly_stays.entity.ReservationStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Reservation> findById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from Reservation r where r.status = :status and r.holdExpiryTime < :expiryTime")
    List<Reservation> findByStatusAndHoldExpiryTimeBefore(@Param("status") ReservationStatus status,
                                                          @Param("expiryTime") LocalDateTime expiryTime);
}
