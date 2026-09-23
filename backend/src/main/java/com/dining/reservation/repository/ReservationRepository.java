package com.dining.reservation.repository;

import com.dining.reservation.domain.Reservation;
import com.dining.reservation.domain.ReservationStatus;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationNumber(String reservationNumber);

    @EntityGraph(attributePaths = {"customer", "assignedTable"})
    Optional<Reservation> findWithDetailsById(Long id);

    @EntityGraph(attributePaths = {"customer", "assignedTable"})
    List<Reservation> findByCustomerIdOrderByReservationDateDescStartTimeDesc(Long customerId);

    @EntityGraph(attributePaths = {"customer", "assignedTable"})
    List<Reservation> findByReservationDateOrderByStartTimeAsc(LocalDate date);

    @EntityGraph(attributePaths = {"customer", "assignedTable"})
    List<Reservation> findByReservationDateGreaterThanEqualAndStatusInOrderByReservationDateAscStartTimeAsc(
            LocalDate date, List<ReservationStatus> statuses);

    @EntityGraph(attributePaths = {"customer", "assignedTable"})
    List<Reservation> findAllByOrderByReservationDateDescStartTimeDesc();

    @Query("""
            SELECT r FROM Reservation r
            WHERE r.assignedTable.id = :tableId
              AND r.reservationDate = :date
              AND r.status IN :activeStatuses
              AND r.startTime < :endTime
              AND r.endTime > :startTime
            """)
    List<Reservation> findOverlapping(
            @Param("tableId") Long tableId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("activeStatuses") List<ReservationStatus> activeStatuses);

    @Query("""
            SELECT r FROM Reservation r
            WHERE r.assignedTable.id = :tableId
              AND r.reservationDate = :date
              AND r.status IN :activeStatuses
              AND r.startTime < :endTime
              AND r.endTime > :startTime
              AND r.id <> :excludeId
            """)
    List<Reservation> findOverlappingExcluding(
            @Param("tableId") Long tableId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("excludeId") Long excludeId,
            @Param("activeStatuses") List<ReservationStatus> activeStatuses);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT r FROM Reservation r WHERE r.id = :id")
    Optional<Reservation> findByIdForUpdate(@Param("id") Long id);

    long countByReservationDateAndStatusIn(LocalDate date, List<ReservationStatus> statuses);

    long countByStatus(ReservationStatus status);
}
