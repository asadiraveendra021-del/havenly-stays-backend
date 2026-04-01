package com.asadi.havenly_stays.service.impl;

import com.asadi.havenly_stays.entity.Hotel;
import com.asadi.havenly_stays.entity.MealPlan;
import com.asadi.havenly_stays.entity.Reservation;
import com.asadi.havenly_stays.entity.ReservationStatus;
import com.asadi.havenly_stays.entity.RoomAvailability;
import com.asadi.havenly_stays.entity.RoomType;
import com.asadi.havenly_stays.entity.User;
import com.asadi.havenly_stays.exception.ReservationException;
import com.asadi.havenly_stays.exception.ResourceNotFoundException;
import com.asadi.havenly_stays.repository.HotelRepository;
import com.asadi.havenly_stays.repository.MealPlanRepository;
import com.asadi.havenly_stays.repository.ReservationRepository;
import com.asadi.havenly_stays.repository.RoomAvailabilityRepository;
import com.asadi.havenly_stays.repository.RoomTypeRepository;
import com.asadi.havenly_stays.repository.UserRepository;
import com.asadi.havenly_stays.service.ReservationService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReservationServiceImpl implements ReservationService {

    private static final Logger logger = LoggerFactory.getLogger(ReservationServiceImpl.class);

    private final ReservationRepository reservationRepository;
    private final RoomAvailabilityRepository roomAvailabilityRepository;
    private final RoomTypeRepository roomTypeRepository;
    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final MealPlanRepository mealPlanRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  RoomAvailabilityRepository roomAvailabilityRepository,
                                  RoomTypeRepository roomTypeRepository,
                                  HotelRepository hotelRepository,
                                  UserRepository userRepository,
                                  MealPlanRepository mealPlanRepository) {
        this.reservationRepository = reservationRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.mealPlanRepository = mealPlanRepository;
    }

    @Override
    @Transactional
    public Reservation preBook(Long userId,
                               Long roomTypeId,
                               LocalDate checkInDate,
                               LocalDate checkOutDate,
                               Integer roomsRequired,
                               Long mealPlanId) {
        validatePreBookRequest(userId, roomTypeId, checkInDate, checkOutDate, roomsRequired, mealPlanId);

        User user = getUser(userId);
        RoomType roomType = getRoomType(roomTypeId);
        Hotel hotel = getHotel(roomType.getHotelId());

        List<RoomAvailability> availabilities = lockAvailabilities(roomTypeId, checkInDate, checkOutDate);
        int totalNights = (int) ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        validateAvailability(availabilities, totalNights, roomsRequired);

        double roomPrice = availabilities.stream()
                .map(RoomAvailability::getPrice)
                .filter(Objects::nonNull)
                .mapToDouble(price -> price * roomsRequired)
                .sum();

        MealPlan mealPlan = getActiveMealPlanForRoom(roomTypeId, mealPlanId);
        double mealPlanPrice = mealPlan.getPricePerDay() * roomsRequired * totalNights;
        Long selectedMealPlanId = mealPlan.getId();

        double totalPrice = roomPrice + mealPlanPrice;

        availabilities.forEach(availability ->
                availability.setAvailableRooms(availability.getAvailableRooms() - roomsRequired));
        roomAvailabilityRepository.saveAll(availabilities);

        Reservation reservation = Reservation.builder()
                .userId(user.getId())
                .hotelId(hotel.getId())
                .roomTypeId(roomType.getId())
                .mealPlanId(selectedMealPlanId)
                .checkInDate(checkInDate)
                .checkOutDate(checkOutDate)
                .roomsBooked(roomsRequired)
                .totalPrice(totalPrice)
                .status(ReservationStatus.PRE_BOOK)
                .holdExpiryTime(LocalDateTime.now().plusMinutes(15))
                .build();

        Reservation saved = reservationRepository.save(reservation);
        logger.info("Created reservation pre-book id={}, userId={}, roomTypeId={}", saved.getId(), userId, roomTypeId);
        return saved;
    }

    @Override
    @Transactional
    public Reservation confirmReservation(Long reservationId) {
        Reservation reservation = getReservationForUpdate(reservationId);

        if (reservation.getStatus() != ReservationStatus.PRE_BOOK) {
            throw new ReservationException("Reservation is not in PRE_BOOK status");
        }

        if (isHoldExpired(reservation)) {
            expireSingleReservation(reservation);
            throw new ReservationException("Reservation hold has expired");
        }

        reservation.setStatus(ReservationStatus.CONFIRMED);
        Reservation saved = reservationRepository.save(reservation);
        logger.info("Confirmed reservation id={}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public Reservation cancelReservation(Long reservationId) {
        Reservation reservation = getReservationForUpdate(reservationId);

        if (reservation.getStatus() == ReservationStatus.CANCELLED
                || reservation.getStatus() == ReservationStatus.EXPIRED) {
            throw new ReservationException("Reservation is already cancelled or expired");
        }

        restoreInventory(reservation);
        reservation.setStatus(ReservationStatus.CANCELLED);
        Reservation saved = reservationRepository.save(reservation);
        logger.info("Cancelled reservation id={}", saved.getId());
        return saved;
    }

    @Override
    @Transactional
    public void expireReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<Reservation> expired = reservationRepository
                .findByStatusAndHoldExpiryTimeBefore(ReservationStatus.PRE_BOOK, now);
        if (expired.isEmpty()) {
            return;
        }

        for (Reservation reservation : expired) {
            expireSingleReservation(reservation);
        }

        logger.info("Expired {} reservations", expired.size());
    }

    private void validatePreBookRequest(Long userId,
                                        Long roomTypeId,
                                        LocalDate checkInDate,
                                        LocalDate checkOutDate,
                                        Integer roomsRequired,
                                        Long mealPlanId) {
        if (userId == null || roomTypeId == null || checkInDate == null || checkOutDate == null
                || roomsRequired == null || mealPlanId == null) {
            throw new ReservationException("Invalid pre-book request");
        }
        if (!checkInDate.isBefore(checkOutDate)) {
            throw new ReservationException("checkInDate must be before checkOutDate");
        }
        long days = ChronoUnit.DAYS.between(checkInDate, checkOutDate);
        if (days > 30) {
            throw new ReservationException("date range should not exceed 30 days");
        }
        if (roomsRequired <= 0) {
            throw new ReservationException("roomsRequired must be greater than 0");
        }
    }

    private void validateAvailability(List<RoomAvailability> availabilities, int totalNights, int roomsRequired) {
        if (availabilities == null || availabilities.isEmpty()) {
            throw new ReservationException("No availability found for the selected dates");
        }
        long distinctDates = availabilities.stream()
                .map(RoomAvailability::getDate)
                .distinct()
                .count();
        if (distinctDates != totalNights) {
            throw new ReservationException("Availability missing for some dates");
        }
        List<LocalDate> insufficientDates = availabilities.stream()
                .filter(availability -> availability.getAvailableRooms() == null
                        || availability.getAvailableRooms() < roomsRequired)
                .map(RoomAvailability::getDate)
                .distinct()
                .collect(Collectors.toList());
        if (!insufficientDates.isEmpty()) {
            throw new ReservationException("Not enough rooms available for dates: " + insufficientDates);
        }
    }

    private Reservation getReservationForUpdate(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("Reservation not found: " + reservationId));
    }

    private List<RoomAvailability> lockAvailabilities(Long roomTypeId, LocalDate checkInDate, LocalDate checkOutDate) {
        return roomAvailabilityRepository.findByRoomTypeIdAndDateRangeForUpdate(roomTypeId, checkInDate, checkOutDate);
    }

    private void restoreInventory(Reservation reservation) {
        List<RoomAvailability> availabilities = lockAvailabilities(
                reservation.getRoomTypeId(), reservation.getCheckInDate(), reservation.getCheckOutDate());
        int totalNights = (int) ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());
        validateAvailability(availabilities, totalNights, 0);

        availabilities.forEach(availability ->
                availability.setAvailableRooms(availability.getAvailableRooms() + reservation.getRoomsBooked()));
        roomAvailabilityRepository.saveAll(availabilities);
    }

    private void expireSingleReservation(Reservation reservation) {
        if (reservation.getStatus() != ReservationStatus.PRE_BOOK) {
            return;
        }
        restoreInventory(reservation);
        reservation.setStatus(ReservationStatus.EXPIRED);
        reservationRepository.save(reservation);
    }

    private boolean isHoldExpired(Reservation reservation) {
        return reservation.getHoldExpiryTime() != null
                && reservation.getHoldExpiryTime().isBefore(LocalDateTime.now());
    }

    private RoomType getRoomType(Long roomTypeId) {
        return roomTypeRepository.findById(roomTypeId)
                .orElseThrow(() -> new ResourceNotFoundException("Room type not found: " + roomTypeId));
    }

    private Hotel getHotel(Long hotelId) {
        return hotelRepository.findById(hotelId)
                .filter(hotel -> Boolean.FALSE.equals(hotel.getIsDeleted()))
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found: " + hotelId));
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
    }

    private MealPlan getActiveMealPlanForRoom(Long roomTypeId, Long mealPlanId) {
        MealPlan mealPlan = mealPlanRepository.findByIdAndRoomTypeId(mealPlanId, roomTypeId)
                .orElseThrow(() -> new ReservationException("Meal plan not found for room type"));
        if (!Boolean.TRUE.equals(mealPlan.getIsActive())) {
            throw new ReservationException("Meal plan is not active");
        }
        return mealPlan;
    }
}
