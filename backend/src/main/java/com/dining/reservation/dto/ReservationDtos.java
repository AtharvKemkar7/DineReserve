package com.dining.reservation.dto;

import com.dining.reservation.domain.Reservation;
import com.dining.reservation.domain.ReservationStatus;
import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

public class ReservationDtos {

    public record CreateReservationRequest(
            @NotNull @FutureOrPresent LocalDate reservationDate,
            @NotNull LocalTime startTime,
            @NotNull @Min(1) Integer guestCount,
            Integer durationMinutes,
            Long preferredTableId,
            String specialRequest
    ) {}

    public record RescheduleRequest(
            @NotNull @FutureOrPresent LocalDate reservationDate,
            @NotNull LocalTime startTime,
            Integer durationMinutes,
            Integer guestCount
    ) {}

    public record AssignTableRequest(@NotNull Long tableId) {}

    public record ManagerNoteRequest(String note) {}

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ReservationResponse(
            Long id,
            String reservationNumber,
            Long customerId,
            String customerName,
            String customerEmail,
            LocalDate reservationDate,
            LocalTime startTime,
            LocalTime endTime,
            int guestCount,
            ReservationStatus status,
            Long tableId,
            String tableNumber,
            String specialRequest,
            String managerNote,
            Instant createdAt,
            Instant updatedAt,
            Instant checkedInAt,
            Instant completedAt
    ) {
        public static ReservationResponse from(Reservation r, boolean includeManagerNote) {
            return new ReservationResponse(
                    r.getId(),
                    r.getReservationNumber(),
                    r.getCustomer().getId(),
                    r.getCustomer().getFullName(),
                    r.getCustomer().getEmail(),
                    r.getReservationDate(),
                    r.getStartTime(),
                    r.getEndTime(),
                    r.getGuestCount(),
                    r.getStatus(),
                    r.getAssignedTable() != null ? r.getAssignedTable().getId() : null,
                    r.getAssignedTable() != null ? r.getAssignedTable().getTableNumber() : null,
                    r.getSpecialRequest(),
                    includeManagerNote ? r.getManagerNote() : null,
                    r.getCreatedAt(),
                    r.getUpdatedAt(),
                    r.getCheckedInAt(),
                    r.getCompletedAt()
            );
        }

        public static ReservationResponse forCustomer(Reservation r) {
            return from(r, false);
        }

        public static ReservationResponse forStaff(Reservation r) {
            return from(r, true);
        }
    }

    public record AvailabilitySlot(
            LocalTime startTime,
            LocalTime endTime,
            int availableTableCount,
            boolean available
    ) {}

    public record AvailabilityResponse(
            LocalDate date,
            int guestCount,
            java.util.List<TableDtos.TableResponse> availableTables,
            java.util.List<AvailabilitySlot> slots
    ) {}

    public record DashboardStats(
            long todayReservations,
            long upcomingConfirmed,
            long pendingCount,
            long completedToday,
            long noShowToday,
            long cancelledToday
    ) {}
}
