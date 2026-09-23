package com.dining.reservation.service;

import com.dining.reservation.domain.Feedback;
import com.dining.reservation.domain.Reservation;
import com.dining.reservation.domain.ReservationStatus;
import com.dining.reservation.dto.FeedbackDtos.FeedbackRequest;
import com.dining.reservation.exception.ApiException;
import com.dining.reservation.repository.FeedbackRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final ReservationService reservationService;

    public FeedbackService(FeedbackRepository feedbackRepository, ReservationService reservationService) {
        this.feedbackRepository = feedbackRepository;
        this.reservationService = reservationService;
    }

    @Transactional
    public Feedback create(Long reservationId, Long customerId, FeedbackRequest request) {
        Reservation reservation = reservationService.getById(reservationId);
        if (!reservation.getCustomer().getId().equals(customerId)) {
            throw ApiException.forbidden("You can only leave feedback on your own reservations");
        }
        if (reservation.getStatus() != ReservationStatus.COMPLETED) {
            throw ApiException.badRequest("Feedback is only allowed after a completed visit");
        }
        if (feedbackRepository.existsByReservationId(reservationId)) {
            throw ApiException.conflict("Feedback already submitted");
        }
        Feedback feedback = new Feedback();
        feedback.setReservation(reservation);
        feedback.setCustomer(reservation.getCustomer());
        feedback.setRating(request.rating());
        feedback.setComment(request.comment());
        return feedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)
    public Feedback getByReservation(Long reservationId) {
        return feedbackRepository.findByReservationId(reservationId)
                .orElseThrow(() -> ApiException.notFound("Feedback not found"));
    }
}
