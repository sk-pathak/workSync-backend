package org.openlake.workSync.app.controller;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.dto.NotificationResponseDTO;
import org.openlake.workSync.app.service.NotificationService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.openlake.workSync.app.dto.PagedResponse;
import org.openlake.workSync.app.repo.NotificationRepo;

import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final NotificationRepo notificationRepo;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponse<NotificationResponseDTO>> listNotifications(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(notificationService.listNotifications(user.getId(), pageable));
    }

    @GetMapping("/dismissed")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponse<NotificationResponseDTO>> listDismissedNotifications(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(notificationService.listDismissedNotifications(user.getId(), pageable));
    }

    @GetMapping("/unread-count")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Long>> getUnreadCount(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user) {
        long count = notificationService.getUnreadCount(user.getId());
        return ResponseEntity.ok(Map.of("unreadCount", count));
    }

    @PostMapping("/{id}/read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAsRead(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @PathVariable UUID id) {
        notificationService.markAsRead(user.getId(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/dismiss")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> dismiss(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @PathVariable UUID id) {
        notificationService.dismiss(user.getId(), id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mark-all-read")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> markAllAsRead(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user) {
        notificationService.markAllAsRead(user.getId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/dismiss-all")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> dismissAll(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user) {
        notificationService.dismissAll(user.getId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/test")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> test(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user) {
        Map<String, Object> result = new HashMap<>();
        try {
            long totalCount = notificationRepo.count();
            result.put("totalCount", totalCount);
            
            long userCount = notificationRepo.findByRecipientId(user.getId()).size();
            result.put("userCount", userCount);
            
            result.put("status", "success");
        } catch (Exception e) {
            result.put("status", "error");
            result.put("error", e.getMessage());
            result.put("errorType", e.getClass().getSimpleName());
            e.printStackTrace();
        }
        return ResponseEntity.ok(result);
    }
}
