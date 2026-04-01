package com.asadi.havenly_stays.service.impl;

import com.asadi.havenly_stays.dto.ReservationBookingItemRequest;
import com.asadi.havenly_stays.dto.ReservationRequest;
import com.asadi.havenly_stays.entity.Hotel;
import com.asadi.havenly_stays.entity.MealPlan;
import com.asadi.havenly_stays.entity.Reservation;
import com.asadi.havenly_stays.entity.ReservationItem;
import com.asadi.havenly_stays.entity.ReservationStatus;
import com.asadi.havenly_stays.entity.RoomAvailability;
import com.asadi.havenly_stays.entity.RoomType;
import com.asadi.havenly_stays.entity.User;
import com.asadi.havenly_stays.exception.ReservationException;
import com.asadi.havenly_stays.exception.ResourceNotFoundException;
import com.asadi.havenly_stays.repository.HotelRepository;
import com.asadi.havenly_stays.repository.MealPlanRepository;
import com.asadi.havenly_stays.repository.ReservationRepository;
import com.asadi.havenly_stays.repository.ReservationItemRepository;
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
    private final ReservationItemRepository reservationItemRepository;

    public ReservationServiceImpl(ReservationRepository reservationRepository,
                                  RoomAvailabilityRepository roomAvailabilityRepository,
                                  RoomTypeRepository roomTypeRepository,
                                  HotelRepository hotelRepository,
                                  UserRepository userRepository,
                                  MealPlanRepository mealPlanRepository,
                                  ReservationItemRepository reservationItemRepository) {
        this.reservationRepository = reservationRepository;
        this.roomAvailabilityRepository = roomAvailabilityRepository;
        this.roomTypeRepository = roomTypeRepository;
        this.hotelRepository = hotelRepository;
        this.userRepository = userRepository;
        this.mealPlanRepository = mealPlanRepository;
        this.reservationItemRepository = reservationItemRepository;
    }

    @Override
    @Transactional
    public Reservation preBook(ReservationRequest request) {
        validatePreBookRequest(request);

        User user = getUser(request.getUserId());
        Hotel hotel = getHotel(request.getHotelId());

        List<ReservationItem> items = new java.util.ArrayList<>();
        double totalPrice = 0.0;
        double totalRoomPrice = 0.0;
        double totalMealPrice = 0.0;
        Integer totalRoomsBooked = 0;
        Long primaryRoomTypeId = null;
        Long primaryMealPlanId = null;
        LocalDate earliestCheckIn = null;
        LocalDate latestCheckOut = null;

        for (ReservationBookingItemRequest booking : request.getBookings()) {
            RoomType roomType = getRoomType(booking.getRoomTypeId());
            if (!hotel.getId().equals(roomType.getHotelId())) {
                throw new ReservationException("Room type does not belong to the specified hotel");
            }

            List<RoomAvailability> availabilities = lockAvailabilities(
                    booking.getRoomTypeId(), booking.getCheckInDate(), booking.getCheckOutDate());
            int totalNights = (int) ChronoUnit.DAYS.between(booking.getCheckInDate(), booking.getCheckOutDate());
            validateAvailability(availabilities, totalNights, booking.getRoomsRequired());

            double roomPrice = calculateRoomPrice(availabilities, booking.getRoomsRequired());
            double mealPrice = 0.0;
            Long selectedMealPlanId = null;

            if (booking.getMealPlanId() != null) {
                MealPlan mealPlan = getActiveMealPlanForRoom(booking.getRoomTypeId(), booking.getMealPlanId());
                mealPrice = calculateMealPrice(mealPlan.getPricePerDay(), booking.getRoomsRequired(), totalNights);
                selectedMealPlanId = mealPlan.getId();
            }

            double itemTotal = roomPrice + mealPrice;
            totalPrice += itemTotal;
            totalRoomPrice += roomPrice;
            totalMealPrice += mealPrice;
            totalRoomsBooked += booking.getRoomsRequired();

            if (primaryRoomTypeId == null) {
                primaryRoomTypeId = roomType.getId();
            }
            if (primaryMealPlanId == null) {
                primaryMealPlanId = selectedMealPlanId;
            }
            if (earliestCheckIn == null || booking.getCheckInDate().isBefore(earliestCheckIn)) {
                earliestCheckIn = booking.getCheckInDate();
            }
            if (latestCheckOut == null || booking.getCheckOutDate().isAfter(latestCheckOut)) {
                latestCheckOut = booking.getCheckOutDate();
            }

            availabilities.forEach(availability ->
                    availability.setAvailableRooms(availability.getAvailableRooms() - booking.getRoomsRequired()));
            roomAvailabilityRepository.saveAll(availabilities);

            ReservationItem item = ReservationItem.builder()
                    .roomTypeId(roomType.getId())
                    .mealPlanId(selectedMealPlanId)
                    .checkInDate(booking.getCheckInDate())
                    .checkOutDate(booking.getCheckOutDate())
                    .roomsBooked(booking.getRoomsRequired())
                    .roomPrice(roomPrice)
                    .mealPrice(mealPrice)
                    .totalPrice(itemTotal)
                    .build();
            items.add(item);
        }

        Reservation reservation = Reservation.builder()
                .userId(user.getId())
                .hotelId(hotel.getId())
                .roomTypeId(primaryRoomTypeId)
                .mealPlanId(primaryMealPlanId)
                .checkInDate(earliestCheckIn)
                .checkOutDate(latestCheckOut)
                .roomsBooked(totalRoomsBooked)
                .roomPrice(totalRoomPrice)
                .mealPrice(totalMealPrice)
                .totalPrice(totalPrice)
                .status(ReservationStatus.PRE_BOOK)
                .holdExpiryTime(LocalDateTime.now().plusMinutes(30))
                .build();

        Reservation saved = reservationRepository.save(reservation);
        items.forEach(item -> item.setReservationId(saved.getId()));
        reservationItemRepository.saveAll(items);

        logger.info("Created reservation pre-book id={}, userId={}, hotelId={}, items={}",
                saved.getId(), user.getId(), hotel.getId(), items.size());
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

    @Override
    @Transactional(readOnly = true)
    public List<ReservationItem> getReservationItems(Long reservationId) {
        return reservationItemRepository.findByReservationId(reservationId);
    }

    private void validatePreBookRequest(ReservationRequest request) {
        if (request == null || request.getUserId() == null || request.getHotelId() == null
                || request.getBookings() == null || request.getBookings().isEmpty()) {
            throw new ReservationException("Invalid pre-book request");
        }
        for (ReservationBookingItemRequest item : request.getBookings()) {
            if (item.getRoomTypeId() == null || item.getRoomsRequired() == null
                    || item.getCheckInDate() == null || item.getCheckOutDate() == null) {
                throw new ReservationException("Invalid booking item in request");
            }
            if (!item.getCheckInDate().isBefore(item.getCheckOutDate())) {
                throw new ReservationException("checkInDate must be before checkOutDate");
            }
            long days = ChronoUnit.DAYS.between(item.getCheckInDate(), item.getCheckOutDate());
            if (days > 30) {
                throw new ReservationException("date range should not exceed 30 days");
            }
            if (item.getRoomsRequired() <= 0) {
                throw new ReservationException("roomsRequired must be greater than 0");
            }
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
        List<ReservationItem> items = reservationItemRepository.findByReservationId(reservation.getId());
        if (items.isEmpty()) {
            throw new ReservationException("No reservation items found: " + reservation.getId());
        }
        for (ReservationItem item : items) {
            List<RoomAvailability> availabilities = lockAvailabilities(
                    item.getRoomTypeId(), item.getCheckInDate(), item.getCheckOutDate());
            int totalNights = (int) ChronoUnit.DAYS.between(item.getCheckInDate(), item.getCheckOutDate());
            if (availabilities == null || availabilities.isEmpty()) {
                throw new ReservationException("No availability found to restore for reservation: " + reservation.getId());
            }
            long distinctDates = availabilities.stream()
                    .map(RoomAvailability::getDate)
                    .distinct()
                    .count();
            if (distinctDates != totalNights) {
                logger.warn("Reservation {} item restore mismatch: expected {} dates but found {}",
                        reservation.getId(), totalNights, distinctDates);
            }

            availabilities.forEach(availability -> {
                Integer current = availability.getAvailableRooms();
                if (current == null) {
                    current = 0;
                }
                availability.setAvailableRooms(current + item.getRoomsBooked());
            });
            roomAvailabilityRepository.saveAll(availabilities);
        }
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

    private double calculateRoomPrice(List<RoomAvailability> availabilities, int roomsRequired) {
        return availabilities.stream()
                .map(RoomAvailability::getPrice)
                .filter(Objects::nonNull)
                .mapToDouble(price -> price * roomsRequired)
                .sum();
    }

    private double calculateMealPrice(double pricePerDay, int roomsRequired, int totalNights) {
        return pricePerDay * roomsRequired * totalNights;
    }
}
