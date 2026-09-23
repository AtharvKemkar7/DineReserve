package com.dining.reservation.service;

import com.dining.reservation.domain.*;
import com.dining.reservation.dto.ReservationDtos;
import com.dining.reservation.dto.TableDtos;
import com.dining.reservation.exception.ApiException;
import com.dining.reservation.repository.ReservationRepository;
import com.dining.reservation.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final TableService tableService;
    private final RestaurantService restaurantService;
    private final AuditService auditService;
    private final NotificationService notificationService;
    private final ConcurrentHashMap<String, Object> bookingLocks = new ConcurrentHashMap<>();

    public ReservationService(
            ReservationRepository reservationRepository,
            UserRepository userRepository,
            TableService tableService,
            RestaurantService restaurantService,
            AuditService auditService,
            NotificationService notificationService) {
        this.reservationRepository = reservationRepository;
        this.userRepository = userRepository;
        this.tableService = tableService;
        this.restaurantService = restaurantService;
        this.auditService = auditService;
        this.notificationService = notificationService;
    }

    @Transactional
    public Reservation create(Long customerId, ReservationDtos.CreateReservationRequest request) {
        User customer = userRepository.findById(customerId)
                .orElseThrow(() -> ApiException.notFound("Customer not found"));
        RestaurantInfo restaurant = restaurantService.get();
        int duration = request.durationMinutes() == null
                ? restaurant.getDefaultDurationMinutes()
                : request.durationMinutes();
        LocalTime endTime = request.startTime().plusMinutes(duration);
        validateBookingWindow(request.reservationDate(), request.startTime(), endTime, duration, restaurant);
        if (request.guestCount() < 1 || request.guestCount() > 20) {
            throw ApiException.badRequest("Guest count must be between 1 and 20");
        }

        Reservation reservation = new Reservation();
        reservation.setReservationNumber(generateNumber());
        reservation.setCustomer(customer);
        reservation.setReservationDate(request.reservationDate());
        reservation.setStartTime(request.startTime());
        reservation.setEndTime(endTime);
        reservation.setGuestCount(request.guestCount());
        reservation.setSpecialRequest(request.specialRequest());
        reservation.setStatus(ReservationStatus.PENDING);

        if (request.preferredTableId() != null) {
            assignConfirmedTable(reservation, request.preferredTableId(), request.guestCount(),
                    request.reservationDate(), request.startTime(), endTime, null);
        } else {
            List<DiningTable> available = tableService.findAvailable(
                    request.reservationDate(), request.startTime(), endTime, request.guestCount());
            if (available.isEmpty()) {
                throw ApiException.conflict("No tables available for the selected time and party size");
            }
            assignConfirmedTable(reservation, available.get(0).getId(), request.guestCount(),
                    request.reservationDate(), request.startTime(), endTime, null);
        }

        reservation = reservationRepository.save(reservation);
        auditService.record("RESERVATION_CREATED", "Reservation", reservation.getId(),
                reservation.getReservationNumber());
        notificationService.notify(customer,
                "Reservation confirmed",
                "Reservation " + reservation.getReservationNumber() + " is confirmed for "
                        + reservation.getReservationDate() + " at " + reservation.getStartTime() + ".",
                "RESERVATION_CONFIRMED",
                reservation.getId());
        return getById(reservation.getId());
    }

    @Transactional(readOnly = true)
    public Reservation getById(Long id) {
        return reservationRepository.findWithDetailsById(id)
                .orElseThrow(() -> ApiException.notFound("Reservation not found"));
    }

    @Transactional(readOnly = true)
    public List<Reservation> myReservations(Long customerId) {
        return reservationRepository.findByCustomerIdOrderByReservationDateDescStartTimeDesc(customerId);
    }

    @Transactional(readOnly = true)
    public List<Reservation> allReservations() {
        return reservationRepository.findAllByOrderByReservationDateDescStartTimeDesc();
    }

    @Transactional(readOnly = true)
    public List<Reservation> todayReservations() {
        return reservationRepository.findByReservationDateOrderByStartTimeAsc(LocalDate.now());
    }

    @Transactional(readOnly = true)
    public List<Reservation> upcomingReservations() {
        return reservationRepository.findByReservationDateGreaterThanEqualAndStatusInOrderByReservationDateAscStartTimeAsc(
                LocalDate.now(),
                List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN));
    }

    @Transactional
    public Reservation cancel(Long reservationId, Long actorId, boolean staffOverride) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> ApiException.notFound("Reservation not found"));
        assertOwnerOrStaff(reservation, actorId, staffOverride);
        if (reservation.getStatus() == ReservationStatus.CANCELLED) {
            throw ApiException.badRequest("Reservation is already cancelled");
        }
        if (reservation.getStatus() == ReservationStatus.COMPLETED || reservation.getStatus() == ReservationStatus.NO_SHOW) {
            throw ApiException.badRequest("Completed or no-show reservations cannot be cancelled");
        }
        if (!staffOverride) {
            RestaurantInfo restaurant = restaurantService.get();
            LocalDateTime start = LocalDateTime.of(reservation.getReservationDate(), reservation.getStartTime());
            if (start.minusHours(restaurant.getCancellationHoursNotice()).isBefore(LocalDateTime.now())) {
                throw ApiException.badRequest("Cancellations require at least "
                        + restaurant.getCancellationHoursNotice() + " hours notice");
            }
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservation.setCancelledAt(Instant.now());
        reservation = reservationRepository.save(reservation);
        auditService.record("RESERVATION_CANCELLED", "Reservation", reservation.getId(),
                reservation.getReservationNumber());
        notificationService.notify(reservation.getCustomer(),
                "Reservation cancelled",
                "Reservation " + reservation.getReservationNumber() + " was cancelled.",
                "RESERVATION_CANCELLED",
                reservation.getId());
        return getById(reservation.getId());
    }

    @Transactional
    public Reservation reschedule(Long reservationId, Long actorId, ReservationDtos.RescheduleRequest request, boolean staffOverride) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> ApiException.notFound("Reservation not found"));
        assertOwnerOrStaff(reservation, actorId, staffOverride);
        if (reservation.getStatus() != ReservationStatus.PENDING && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw ApiException.badRequest("Only pending or confirmed reservations can be rescheduled");
        }
        RestaurantInfo restaurant = restaurantService.get();
        int duration = request.durationMinutes() == null
                ? restaurant.getDefaultDurationMinutes()
                : request.durationMinutes();
        LocalTime endTime = request.startTime().plusMinutes(duration);
        validateBookingWindow(request.reservationDate(), request.startTime(), endTime, duration, restaurant);
        int guests = request.guestCount() == null ? reservation.getGuestCount() : request.guestCount();
        if (guests < 1) {
            throw ApiException.badRequest("Guest count must be at least 1");
        }

        Long tableIdToLock;
        if (reservation.getAssignedTable() != null) {
            tableIdToLock = reservation.getAssignedTable().getId();
        } else {
            List<DiningTable> available = tableService.findAvailable(
                    request.reservationDate(), request.startTime(), endTime, guests);
            if (available.isEmpty()) {
                throw ApiException.conflict("No tables available for the new time");
            }
            tableIdToLock = available.get(0).getId();
        }
        synchronized (bookingLock(tableIdToLock, request.reservationDate())) {
            DiningTable table = validateTableForBooking(
                    tableIdToLock, guests, request.reservationDate(), request.startTime(), endTime, reservation.getId());
            reservation.setAssignedTable(table);
            reservation.setReservationDate(request.reservationDate());
            reservation.setStartTime(request.startTime());
            reservation.setEndTime(endTime);
            reservation.setGuestCount(guests);
            reservation.setStatus(ReservationStatus.CONFIRMED);
            reservation = reservationRepository.saveAndFlush(reservation);
        }
        auditService.record("RESERVATION_RESCHEDULED", "Reservation", reservation.getId(),
                reservation.getReservationNumber());
        notificationService.notify(reservation.getCustomer(),
                "Reservation rescheduled",
                "Reservation " + reservation.getReservationNumber() + " was moved to "
                        + reservation.getReservationDate() + " at " + reservation.getStartTime() + ".",
                "RESERVATION_RESCHEDULED",
                reservation.getId());
        return getById(reservation.getId());
    }

    @Transactional
    public Reservation assignTable(Long reservationId, Long tableId) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> ApiException.notFound("Reservation not found"));
        if (reservation.getStatus() != ReservationStatus.PENDING && reservation.getStatus() != ReservationStatus.CONFIRMED) {
            throw ApiException.badRequest("Table can only be assigned to pending or confirmed reservations");
        }
        boolean changing = reservation.getAssignedTable() != null
                && !reservation.getAssignedTable().getId().equals(tableId);
        DiningTable table;
        synchronized (bookingLock(tableId, reservation.getReservationDate())) {
            table = validateTableForBooking(
                    tableId,
                    reservation.getGuestCount(),
                    reservation.getReservationDate(),
                    reservation.getStartTime(),
                    reservation.getEndTime(),
                    reservation.getId());
            reservation.setAssignedTable(table);
            if (reservation.getStatus() == ReservationStatus.PENDING) {
                reservation.setStatus(ReservationStatus.CONFIRMED);
            }
            reservation = reservationRepository.saveAndFlush(reservation);
        }
        auditService.record(changing ? "TABLE_CHANGED" : "TABLE_ASSIGNED", "Reservation", reservation.getId(),
                reservation.getReservationNumber() + " -> " + table.getTableNumber());
        notificationService.notify(reservation.getCustomer(),
                "Table assigned",
                "Table " + table.getTableNumber() + " has been assigned to reservation "
                        + reservation.getReservationNumber() + ".",
                "TABLE_ASSIGNED",
                reservation.getId());
        return getById(reservation.getId());
    }

    @Transactional
    public Reservation addNote(Long reservationId, String note) {
        Reservation reservation = getById(reservationId);
        reservation.setManagerNote(note);
        reservationRepository.save(reservation);
        return getById(reservationId);
    }

    @Transactional
    public Reservation checkIn(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> ApiException.notFound("Reservation not found"));
        requireStatus(reservation, ReservationStatus.CONFIRMED, "Only confirmed reservations can be checked in");
        reservation.setStatus(ReservationStatus.CHECKED_IN);
        reservation.setCheckedInAt(Instant.now());
        if (reservation.getAssignedTable() != null) {
            DiningTable table = tableService.getById(reservation.getAssignedTable().getId());
            table.setStatus(TableStatus.OCCUPIED);
        }
        reservation = reservationRepository.save(reservation);
        auditService.record("CUSTOMER_CHECKED_IN", "Reservation", reservation.getId(),
                reservation.getReservationNumber());
        notificationService.notify(reservation.getCustomer(),
                "Checked in",
                "Welcome. Reservation " + reservation.getReservationNumber() + " is checked in.",
                "CHECKED_IN",
                reservation.getId());
        return getById(reservation.getId());
    }

    @Transactional
    public Reservation complete(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> ApiException.notFound("Reservation not found"));
        requireStatus(reservation, ReservationStatus.CHECKED_IN, "Only checked-in reservations can be completed");
        reservation.setStatus(ReservationStatus.COMPLETED);
        reservation.setCompletedAt(Instant.now());
        if (reservation.getAssignedTable() != null) {
            DiningTable table = tableService.getById(reservation.getAssignedTable().getId());
            table.setStatus(TableStatus.AVAILABLE);
        }
        reservation = reservationRepository.save(reservation);
        auditService.record("RESERVATION_COMPLETED", "Reservation", reservation.getId(),
                reservation.getReservationNumber());
        notificationService.notify(reservation.getCustomer(),
                "Visit completed",
                "Thanks for dining with us. You can leave feedback for reservation "
                        + reservation.getReservationNumber() + ".",
                "COMPLETED",
                reservation.getId());
        return getById(reservation.getId());
    }

    @Transactional
    public Reservation markNoShow(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> ApiException.notFound("Reservation not found"));
        if (reservation.getStatus() != ReservationStatus.CONFIRMED && reservation.getStatus() != ReservationStatus.PENDING) {
            throw ApiException.badRequest("Only pending or confirmed reservations can be marked no-show");
        }
        reservation.setStatus(ReservationStatus.NO_SHOW);
        if (reservation.getAssignedTable() != null) {
            DiningTable table = tableService.getById(reservation.getAssignedTable().getId());
            if (table.getStatus() == TableStatus.OCCUPIED) {
                table.setStatus(TableStatus.AVAILABLE);
            }
        }
        reservation = reservationRepository.save(reservation);
        auditService.record("NO_SHOW", "Reservation", reservation.getId(), reservation.getReservationNumber());
        notificationService.notify(reservation.getCustomer(),
                "Marked as no-show",
                "Reservation " + reservation.getReservationNumber() + " was marked as no-show.",
                "NO_SHOW",
                reservation.getId());
        return getById(reservation.getId());
    }

    @Transactional(readOnly = true)
    public ReservationDtos.AvailabilityResponse availability(LocalDate date, LocalTime startTime, Integer guestCount) {
        RestaurantInfo restaurant = restaurantService.get();
        int guests = guestCount == null ? 2 : guestCount;
        LocalTime start = startTime == null ? LocalTime.parse(restaurant.getOpenTime()) : startTime;
        LocalTime end = start.plusMinutes(restaurant.getDefaultDurationMinutes());
        List<DiningTable> tables = tableService.findAvailable(date, start, end, guests);
        List<ReservationDtos.AvailabilitySlot> slots = buildSlots(date, guests, restaurant);
        return new ReservationDtos.AvailabilityResponse(
                date,
                guests,
                tables.stream().map(TableDtos.TableResponse::from).toList(),
                slots
        );
    }

    @Transactional(readOnly = true)
    public ReservationDtos.DashboardStats dashboard() {
        LocalDate today = LocalDate.now();
        List<ReservationStatus> live = List.of(ReservationStatus.PENDING, ReservationStatus.CONFIRMED, ReservationStatus.CHECKED_IN);
        return new ReservationDtos.DashboardStats(
                reservationRepository.countByReservationDateAndStatusIn(today, live),
                reservationRepository.findByReservationDateGreaterThanEqualAndStatusInOrderByReservationDateAscStartTimeAsc(today, live).size(),
                reservationRepository.countByStatus(ReservationStatus.PENDING),
                reservationRepository.countByReservationDateAndStatusIn(today, List.of(ReservationStatus.COMPLETED)),
                reservationRepository.countByReservationDateAndStatusIn(today, List.of(ReservationStatus.NO_SHOW)),
                reservationRepository.countByReservationDateAndStatusIn(today, List.of(ReservationStatus.CANCELLED))
        );
    }

    public void assertOwnerOrStaff(Reservation reservation, Long actorId, boolean staffOverride) {
        if (staffOverride) {
            return;
        }
        if (!reservation.getCustomer().getId().equals(actorId)) {
            throw ApiException.forbidden("You can only access your own reservations");
        }
    }

    private Object bookingLock(Long tableId, LocalDate date) {
        return bookingLocks.computeIfAbsent(tableId + ":" + date, key -> new Object());
    }

    private void assignConfirmedTable(
            Reservation reservation,
            Long tableId,
            int guestCount,
            LocalDate date,
            LocalTime start,
            LocalTime end,
            Long excludeReservationId) {
        synchronized (bookingLock(tableId, date)) {
            DiningTable table = validateTableForBooking(tableId, guestCount, date, start, end, excludeReservationId);
            reservation.setAssignedTable(table);
            reservation.setStatus(ReservationStatus.CONFIRMED);
        }
    }

    private DiningTable lockAndValidateTable(
            Long tableId,
            int guestCount,
            LocalDate date,
            LocalTime start,
            LocalTime end,
            Long excludeReservationId) {
        synchronized (bookingLock(tableId, date)) {
            return validateTableForBooking(tableId, guestCount, date, start, end, excludeReservationId);
        }
    }

    private DiningTable validateTableForBooking(
            Long tableId,
            int guestCount,
            LocalDate date,
            LocalTime start,
            LocalTime end,
            Long excludeReservationId) {
        DiningTable locked = tableService.lockById(tableId);
        if (!locked.isActive() || locked.getStatus() == TableStatus.INACTIVE) {
            throw ApiException.badRequest("Cannot book an inactive table");
        }
        if (locked.getStatus() == TableStatus.MAINTENANCE) {
            throw ApiException.badRequest("Cannot book a table under maintenance");
        }
        if (locked.getCapacity() < guestCount) {
            throw ApiException.badRequest("Table capacity is smaller than guest count");
        }
        boolean free = tableService.isTableFree(locked, date, start, end, excludeReservationId);
        if (!free) {
            throw ApiException.conflict("Table is already booked for the selected time");
        }
        return locked;
    }

    private void validateBookingWindow(
            LocalDate date,
            LocalTime start,
            LocalTime end,
            int duration,
            RestaurantInfo restaurant) {
        if (duration < 30 || duration > 240) {
            throw ApiException.badRequest("Reservation duration must be between 30 and 240 minutes");
        }
        if (!end.isAfter(start)) {
            throw ApiException.badRequest("End time must be after start time");
        }
        LocalTime open = LocalTime.parse(restaurant.getOpenTime());
        LocalTime close = LocalTime.parse(restaurant.getCloseTime());
        if (start.isBefore(open) || end.isAfter(close)) {
            throw ApiException.badRequest("Reservation must be within opening hours "
                    + restaurant.getOpenTime() + "-" + restaurant.getCloseTime());
        }
        LocalDateTime startDateTime = LocalDateTime.of(date, start);
        if (startDateTime.isBefore(LocalDateTime.now())) {
            throw ApiException.badRequest("Cannot book in the past");
        }
    }

    private List<ReservationDtos.AvailabilitySlot> buildSlots(LocalDate date, int guests, RestaurantInfo restaurant) {
        List<ReservationDtos.AvailabilitySlot> slots = new ArrayList<>();
        LocalTime cursor = LocalTime.parse(restaurant.getOpenTime());
        LocalTime close = LocalTime.parse(restaurant.getCloseTime());
        int duration = restaurant.getDefaultDurationMinutes();
        while (!cursor.plusMinutes(duration).isAfter(close)) {
            LocalTime end = cursor.plusMinutes(duration);
            List<DiningTable> available = tableService.findAvailable(date, cursor, end, guests);
            slots.add(new ReservationDtos.AvailabilitySlot(cursor, end, available.size(), !available.isEmpty()));
            cursor = cursor.plusMinutes(30);
        }
        return slots;
    }

    private String generateNumber() {
        String candidate;
        do {
            candidate = "HR" + LocalDate.now().format(DateTimeFormatter.BASIC_ISO_DATE)
                    + String.format("%04d", ThreadLocalRandom.current().nextInt(10000));
        } while (reservationRepository.findByReservationNumber(candidate).isPresent());
        return candidate;
    }

    private void requireStatus(Reservation reservation, ReservationStatus expected, String message) {
        if (reservation.getStatus() != expected) {
            throw ApiException.badRequest(message);
        }
    }
}
