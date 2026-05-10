package com.gestion.gestion_bus.controller;


import com.gestion.gestion_bus.dto.response.DashboardResponse;
import com.gestion.gestion_bus.service.interfaces.DashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/dashboard")
    public ResponseEntity<DashboardResponse> getDashboardData() {
        DashboardResponse response = dashboardService.getDashboardData();
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/system-status")
    public Map<String, Object> getSystemStatus() {
        Map<String, Object> map = new HashMap<>();
        map.put("status", "OPERATIONAL");
        map.put("lastUpdate", Instant.now());
        return map;
    }
}