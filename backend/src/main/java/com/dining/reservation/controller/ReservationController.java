package com.dining.reservation.controller;

import com.dining.reservation.domain.Reservation;
import com.dining.reservation.dto.FeedbackDtos;
import com.dining.reservation.dto.ReservationDtos;
import com.dining.reservation.dto.ReservationDtos.AvailabilityResponse;
import com.dining.reservation.dto.ReservationDtos.CreateReservationRequest;
import com.dining.reservation.dto.ReservationDtos.ReservationResponse;
import com.dining.reservation.dto.ReservationDtos.RescheduleRequest;
import com.dining.reservation.security.SecurityUtil;
import com.dining.reservation.service.FeedbackService;
import com.dining.reservation.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api")
public class ReservationController {

    private final ReservationService reservationService;
    private final FeedbackService feedbackService;

    public ReservationController(ReservationService reservationService, FeedbackService feedbackService) {
        this.reservationService = reservationService;
        this.feedbackService = feedbackService;
    }

    @GetMapping("/tables/availability")
    public AvailabilityResponse availability(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime startTime,
            @RequestParam(required = false) Integer guestCount) {
        return reservationService.availability(date, startTime, guestCount);
    }

    @GetMapping("/reservations/slots")
    public AvailabilityResponse slots(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Integer guestCount) {
        return reservationService.availability(date, null, guestCount);
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody CreateReservationRequest request) {
        Reservation reservation = reservationService.create(SecurityUtil.currentUserId(), request);
        return ReservationResponse.forCustomer(reservationService.getById(reservation.getId()));
    }

    @GetMapping("/reservations/my")
    public List<ReservationResponse> myReservations() {
        return reservationService.myReservations(SecurityUtil.currentUserId()).stream()
                .map(ReservationResponse::forCustomer)
                .toList();
    }

    @GetMapping("/reservations/{id}")
    public ReservationResponse get(@PathVariable Long id) {
        Reservation reservation = reservationService.getById(id);
        boolean staff = SecurityUtil.isStaff();
        reservationService.assertOwnerOrStaff(reservation, SecurityUtil.currentUserId(), staff);
        return staff ? ReservationResponse.forStaff(reservation) : ReservationResponse.forCustomer(reservation);
    }

    @PostMapping("/reservations/{id}/cancel")
    public ReservationResponse cancel(@PathVariable Long id) {
        boolean staff = SecurityUtil.isStaff();
        Reservation reservation = reservationService.cancel(id, SecurityUtil.currentUserId(), staff);
        return staff ? ReservationResponse.forStaff(reservation) : ReservationResponse.forCustomer(reservation);
    }

    @PostMapping("/reservations/{id}/reschedule")
    public ReservationResponse reschedule(@PathVariable Long id, @Valid @RequestBody RescheduleRequest request) {
        boolean staff = SecurityUtil.isStaff();
        Reservation reservation = reservationService.reschedule(id, SecurityUtil.currentUserId(), request, staff);
        return staff ? ReservationResponse.forStaff(reservation) : ReservationResponse.forCustomer(reservation);
    }

    @PostMapping("/reservations/{id}/feedback")
    @ResponseStatus(HttpStatus.CREATED)
    public FeedbackDtos.FeedbackResponse feedback(
            @PathVariable Long id,
            @Valid @RequestBody FeedbackDtos.FeedbackRequest request) {
        return FeedbackDtos.FeedbackResponse.from(
                feedbackService.create(id, SecurityUtil.currentUserId(), request));
    }
}
