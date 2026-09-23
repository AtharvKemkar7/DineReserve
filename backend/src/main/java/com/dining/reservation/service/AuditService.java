package com.dining.reservation.service;

import com.dining.reservation.domain.AuditLog;
import com.dining.reservation.repository.AuditLogRepository;
import com.dining.reservation.security.SecurityUtil;
import com.dining.reservation.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AuditService {

    private final AuditLogRepository auditLogRepository;

    public AuditService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void record(String action, String entityType, Long entityId, String details) {
        AuditLog log = new AuditLog();
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setDetails(details);
        try {
            UserPrincipal principal = SecurityUtil.currentUser();
            log.setActorId(principal.getId());
            log.setActorEmail(principal.getUsername());
        } catch (Exception ignored) {
            log.setActorEmail("system");
        }
        auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public List<AuditLog> findAll() {
        return auditLogRepository.findAllByOrderByCreatedAtDesc();
    }
}
