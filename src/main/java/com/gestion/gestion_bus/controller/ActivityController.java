package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.dto.ActivityDto;
import com.gestion.gestion_bus.service.interfaces.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/activities")
@RequiredArgsConstructor
public class ActivityController {
    private final ActivityService activityService;

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{userId}")
    public List<ActivityDto> getRecentActivities(@PathVariable Long userId) {
        return activityService.getRecentActivities(userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/recent")
    public List<ActivityDto> getRecentActivitiesAdmin() {
        return activityService.getRecentActivitiesAdmin();
    }
}
