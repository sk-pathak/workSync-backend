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

import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<PagedResponse<NotificationResponseDTO>> listNotifications(@AuthenticationPrincipal org.openlake.workSync.app.domain.entity.User user, @PageableDefault Pageable pageable) {
        return ResponseEntity.ok(notificationService.listNotifications(user.getId(), pageable));
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
}
