package com.asadi.havenly_stays.service;

import com.asadi.havenly_stays.entity.Reservation;
import java.time.LocalDate;

public interface ReservationService {
    Reservation preBook(Long userId,
                        Long roomTypeId,
                        LocalDate checkInDate,
                        LocalDate checkOutDate,
                        Integer roomsRequired,
                        Long mealPlanId);

    Reservation confirmReservation(Long reservationId);

    Reservation cancelReservation(Long reservationId);

    void expireReservations();
}
