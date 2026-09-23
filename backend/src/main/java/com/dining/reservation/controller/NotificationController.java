package com.dining.reservation.controller;

import com.dining.reservation.dto.NotificationDtos.NotificationResponse;
import com.dining.reservation.security.SecurityUtil;
import com.dining.reservation.service.NotificationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping
    public List<NotificationResponse> list() {
        return notificationService.listForUser(SecurityUtil.currentUserId()).stream()
                .map(NotificationResponse::from)
                .toList();
    }

    @GetMapping("/unread-count")
    public Map<String, Long> unread() {
        return Map.of("count", notificationService.unreadCount(SecurityUtil.currentUserId()));
    }

    @PostMapping("/{id}/read")
    public NotificationResponse markRead(@PathVariable Long id) {
        return NotificationResponse.from(notificationService.markRead(id, SecurityUtil.currentUserId()));
    }
}
