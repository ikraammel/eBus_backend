package com.gestion.gestion_bus.controller;


import com.gestion.gestion_bus.dto.response.DashboardResponse;
import com.gestion.gestion_bus.service.imp.DashboardServiceImp;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardServiceImp dashboardService;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboardData() {
        DashboardResponse response = dashboardService.getDashboardData();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/dashboard/refresh")
    public ResponseEntity<DashboardResponse> refreshDashboard() {
        DashboardResponse response = dashboardService.getDashboardData();
        return ResponseEntity.ok(response);
    }
}