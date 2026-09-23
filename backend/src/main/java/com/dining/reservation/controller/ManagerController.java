package com.dining.reservation.controller;

import com.dining.reservation.domain.TableStatus;
import com.dining.reservation.dto.ReservationDtos;
import com.dining.reservation.dto.ReservationDtos.AssignTableRequest;
import com.dining.reservation.dto.ReservationDtos.DashboardStats;
import com.dining.reservation.dto.ReservationDtos.ManagerNoteRequest;
import com.dining.reservation.dto.ReservationDtos.ReservationResponse;
import com.dining.reservation.dto.TableDtos;
import com.dining.reservation.dto.TableDtos.TableStatusRequest;
import com.dining.reservation.service.ReservationService;
import com.dining.reservation.service.TableService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
public class ManagerController {

    private final ReservationService reservationService;
    private final TableService tableService;

    public ManagerController(ReservationService reservationService, TableService tableService) {
        this.reservationService = reservationService;
        this.tableService = tableService;
    }

    @GetMapping("/dashboard")
    public DashboardStats dashboard() {
        return reservationService.dashboard();
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> upcoming() {
        return reservationService.upcomingReservations().stream()
                .map(ReservationResponse::forStaff)
                .toList();
    }

    @GetMapping("/reservations/today")
    public List<ReservationResponse> today() {
        return reservationService.todayReservations().stream()
                .map(ReservationResponse::forStaff)
                .toList();
    }

    @GetMapping("/reservations/{id}")
    public ReservationResponse get(@PathVariable Long id) {
        return ReservationResponse.forStaff(reservationService.getById(id));
    }

    @PostMapping("/reservations/{id}/assign-table")
    public ReservationResponse assign(@PathVariable Long id, @Valid @RequestBody AssignTableRequest request) {
        return ReservationResponse.forStaff(reservationService.assignTable(id, request.tableId()));
    }

    @PostMapping("/reservations/{id}/check-in")
    public ReservationResponse checkIn(@PathVariable Long id) {
        return ReservationResponse.forStaff(reservationService.checkIn(id));
    }

    @PostMapping("/reservations/{id}/complete")
    public ReservationResponse complete(@PathVariable Long id) {
        return ReservationResponse.forStaff(reservationService.complete(id));
    }

    @PostMapping("/reservations/{id}/no-show")
    public ReservationResponse noShow(@PathVariable Long id) {
        return ReservationResponse.forStaff(reservationService.markNoShow(id));
    }

    @PostMapping("/reservations/{id}/cancel")
    public ReservationResponse cancel(@PathVariable Long id) {
        return ReservationResponse.forStaff(reservationService.cancel(id, null, true));
    }

    @PostMapping("/reservations/{id}/reschedule")
    public ReservationResponse reschedule(
            @PathVariable Long id,
            @Valid @RequestBody ReservationDtos.RescheduleRequest request) {
        return ReservationResponse.forStaff(reservationService.reschedule(id, null, request, true));
    }

    @PostMapping("/reservations/{id}/notes")
    public ReservationResponse notes(@PathVariable Long id, @RequestBody ManagerNoteRequest request) {
        return ReservationResponse.forStaff(reservationService.addNote(id, request.note()));
    }

    @GetMapping("/tables")
    public List<TableDtos.TableResponse> tables() {
        return tableService.findOperational().stream().map(TableDtos.TableResponse::from).toList();
    }

    @PatchMapping("/tables/{id}/status")
    public TableDtos.TableResponse updateStatus(@PathVariable Long id, @Valid @RequestBody TableStatusRequest request) {
        TableStatus status = request.status();
        return TableDtos.TableResponse.from(tableService.updateOperationalStatus(id, status));
    }
}
