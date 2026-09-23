package com.dining.reservation.service;

import com.dining.reservation.domain.DiningTable;
import com.dining.reservation.domain.Reservation;
import com.dining.reservation.domain.ReservationStatus;
import com.dining.reservation.domain.TableStatus;
import com.dining.reservation.dto.TableDtos.TableRequest;
import com.dining.reservation.exception.ApiException;
import com.dining.reservation.repository.DiningTableRepository;
import com.dining.reservation.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
public class TableService {

    public static final List<ReservationStatus> ACTIVE_RESERVATION_STATUSES = List.of(
            ReservationStatus.PENDING,
            ReservationStatus.CONFIRMED,
            ReservationStatus.CHECKED_IN
    );

    private final DiningTableRepository tableRepository;
    private final ReservationRepository reservationRepository;
    private final AuditService auditService;

    public TableService(
            DiningTableRepository tableRepository,
            ReservationRepository reservationRepository,
            AuditService auditService) {
        this.tableRepository = tableRepository;
        this.reservationRepository = reservationRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public List<DiningTable> findAll() {
        return tableRepository.findAllByOrderByTableNumberAsc();
    }

    @Transactional(readOnly = true)
    public List<DiningTable> findOperational() {
        return tableRepository.findByActiveTrueOrderByTableNumberAsc();
    }

    @Transactional(readOnly = true)
    public DiningTable getById(Long id) {
        return tableRepository.findById(id).orElseThrow(() -> ApiException.notFound("Table not found"));
    }

    @Transactional
    public DiningTable lockById(Long id) {
        return tableRepository.findByIdForUpdate(id).orElseThrow(() -> ApiException.notFound("Table not found"));
    }

    @Transactional
    public DiningTable create(TableRequest request) {
        if (tableRepository.existsByTableNumber(request.tableNumber())) {
            throw ApiException.conflict("Table number already exists");
        }
        DiningTable table = new DiningTable();
        apply(table, request, true);
        table = tableRepository.save(table);
        auditService.record("TABLE_CREATED", "DiningTable", table.getId(), table.getTableNumber());
        return table;
    }

    @Transactional
    public DiningTable update(Long id, TableRequest request) {
        DiningTable table = getById(id);
        if (!table.getTableNumber().equals(request.tableNumber()) && tableRepository.existsByTableNumber(request.tableNumber())) {
            throw ApiException.conflict("Table number already exists");
        }
        apply(table, request, false);
        table = tableRepository.save(table);
        auditService.record("TABLE_UPDATED", "DiningTable", table.getId(), table.getTableNumber());
        return table;
    }

    @Transactional
    public void delete(Long id) {
        DiningTable table = getById(id);
        tableRepository.delete(table);
        auditService.record("TABLE_DELETED", "DiningTable", id, table.getTableNumber());
    }

    @Transactional
    public DiningTable updateOperationalStatus(Long id, TableStatus status) {
        if (status == TableStatus.INACTIVE) {
            throw ApiException.badRequest("Managers cannot deactivate tables. Ask an admin.");
        }
        DiningTable table = tableRepository.findByIdForUpdate(id)
                .orElseThrow(() -> ApiException.notFound("Table not found"));
        if (!table.isActive()) {
            throw ApiException.badRequest("Inactive tables cannot be operated");
        }
        table.setStatus(status);
        table = tableRepository.save(table);
        auditService.record("TABLE_STATUS_CHANGED", "DiningTable", table.getId(),
                table.getTableNumber() + " -> " + status);
        return table;
    }

    @Transactional(readOnly = true)
    public boolean isTableFree(DiningTable table, LocalDate date, LocalTime start, LocalTime end, Long excludeReservationId) {
        if (!table.isActive() || table.getStatus() == TableStatus.MAINTENANCE || table.getStatus() == TableStatus.INACTIVE) {
            return false;
        }
        List<Reservation> overlaps;
        if (excludeReservationId == null) {
            overlaps = reservationRepository.findOverlapping(table.getId(), date, start, end, ACTIVE_RESERVATION_STATUSES);
        } else {
            overlaps = reservationRepository.findOverlappingExcluding(
                    table.getId(), date, start, end, excludeReservationId, ACTIVE_RESERVATION_STATUSES);
        }
        return overlaps.isEmpty();
    }

    @Transactional(readOnly = true)
    public List<DiningTable> findAvailable(LocalDate date, LocalTime start, LocalTime end, int guestCount) {
        return tableRepository.findByActiveTrueOrderByTableNumberAsc().stream()
                .filter(t -> t.getStatus() == TableStatus.AVAILABLE || t.getStatus() == TableStatus.OCCUPIED)
                .filter(t -> t.getCapacity() >= guestCount)
                .filter(t -> isTableFree(t, date, start, end, null))
                .toList();
    }

    private void apply(DiningTable table, TableRequest request, boolean creating) {
        table.setTableNumber(request.tableNumber());
        table.setName(request.name());
        table.setCapacity(request.capacity());
        table.setSection(request.section());
        if (request.status() != null) {
            table.setStatus(request.status());
        } else if (creating) {
            table.setStatus(TableStatus.AVAILABLE);
        }
        if (request.active() != null) {
            table.setActive(request.active());
            if (!request.active()) {
                table.setStatus(TableStatus.INACTIVE);
            } else if (table.getStatus() == TableStatus.INACTIVE) {
                table.setStatus(TableStatus.AVAILABLE);
            }
        }
    }
}
