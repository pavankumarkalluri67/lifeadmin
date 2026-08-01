package com.LifeAdmin.ai.lifeadmin.dashboard.api;


import com.LifeAdmin.ai.lifeadmin.dashboard.application.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DashboardController: provides dashboard summary.
 * Requirement 21
 */
@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/summary")
    public ResponseEntity<DashboardService.DashboardSummary> getSummary() {
        DashboardService.DashboardSummary summary = dashboardService.summary();
        return ResponseEntity.ok(summary);
    }
}
