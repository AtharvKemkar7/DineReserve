package com.dining.reservation.dto;

import com.dining.reservation.domain.AuditLog;

import java.time.Instant;

public class AuditDtos {

    public record AuditLogResponse(
            Long id,
            String action,
            String entityType,
            Long entityId,
            Long actorId,
            String actorEmail,
            String details,
            Instant createdAt
    ) {
        public static AuditLogResponse from(AuditLog log) {
            return new AuditLogResponse(
                    log.getId(),
                    log.getAction(),
                    log.getEntityType(),
                    log.getEntityId(),
                    log.getActorId(),
                    log.getActorEmail(),
                    log.getDetails(),
                    log.getCreatedAt()
            );
        }
    }
}
