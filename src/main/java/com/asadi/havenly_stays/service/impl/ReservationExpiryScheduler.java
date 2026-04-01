package com.asadi.havenly_stays.service.impl;

import com.asadi.havenly_stays.service.ReservationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ReservationExpiryScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ReservationExpiryScheduler.class);

    private final ReservationService reservationService;

    public ReservationExpiryScheduler(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Scheduled(cron = "0 */5 * * * *")
    public void expireReservations() {
        try {
            reservationService.expireReservations();
        } catch (Exception ex) {
            logger.error("Failed to expire reservations", ex);
        }
    }
}
