package com.dining.reservation.repository;

import com.dining.reservation.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findByReservationId(Long reservationId);
    boolean existsByReservationId(Long reservationId);
}
