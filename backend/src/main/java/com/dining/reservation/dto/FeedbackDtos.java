package com.dining.reservation.dto;

import com.dining.reservation.domain.Feedback;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class FeedbackDtos {

    public record FeedbackRequest(
            @NotNull @Min(1) @Max(5) Integer rating,
            String comment
    ) {}

    public record FeedbackResponse(
            Long id,
            Long reservationId,
            int rating,
            String comment,
            Instant createdAt
    ) {
        public static FeedbackResponse from(Feedback f) {
            return new FeedbackResponse(
                    f.getId(),
                    f.getReservation().getId(),
                    f.getRating(),
                    f.getComment(),
                    f.getCreatedAt()
            );
        }
    }
}
