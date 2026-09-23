package com.dining.reservation.repository;

import com.dining.reservation.domain.DiningTable;
import com.dining.reservation.domain.TableStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface DiningTableRepository extends JpaRepository<DiningTable, Long> {
    boolean existsByTableNumber(String tableNumber);
    List<DiningTable> findByActiveTrueAndStatus(TableStatus status);
    List<DiningTable> findAllByOrderByTableNumberAsc();
    List<DiningTable> findByActiveTrueOrderByTableNumberAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM DiningTable t WHERE t.id = :id")
    Optional<DiningTable> findByIdForUpdate(@Param("id") Long id);
}
