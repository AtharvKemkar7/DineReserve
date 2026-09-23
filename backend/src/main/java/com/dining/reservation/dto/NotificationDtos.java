package com.dining.reservation.dto;

import com.dining.reservation.domain.Notification;

import java.time.Instant;

public class NotificationDtos {

    public record NotificationResponse(
            Long id,
            String title,
            String message,
            String type,
            Long reservationId,
            boolean read,
            Instant createdAt
    ) {
        public static NotificationResponse from(Notification n) {
            return new NotificationResponse(
                    n.getId(),
                    n.getTitle(),
                    n.getMessage(),
                    n.getType(),
                    n.getReservationId(),
                    n.isReadFlag(),
                    n.getCreatedAt()
            );
        }
    }
}
