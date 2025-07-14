package org.openlake.workSync.app.controller;

import lombok.RequiredArgsConstructor;
import org.openlake.workSync.app.dto.GeneralAnalyticsDTO;
import org.openlake.workSync.app.service.AnalyticsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/analytics")
@RequiredArgsConstructor
public class AnalyticsController {
    private final AnalyticsService analyticsService;

    @GetMapping("/overview")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<GeneralAnalyticsDTO> getGeneralAnalytics() {
        return ResponseEntity.ok(analyticsService.getGeneralAnalytics());
    }
}