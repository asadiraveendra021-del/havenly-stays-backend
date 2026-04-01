package com.asadi.havenly_stays.service;

import com.asadi.havenly_stays.dto.ReservationRequest;
import com.asadi.havenly_stays.entity.Reservation;
import com.asadi.havenly_stays.entity.ReservationItem;
import java.util.List;

public interface ReservationService {
    Reservation preBook(ReservationRequest request);

    Reservation confirmReservation(Long reservationId);

    Reservation cancelReservation(Long reservationId);

    void expireReservations();

    List<ReservationItem> getReservationItems(Long reservationId);
}
