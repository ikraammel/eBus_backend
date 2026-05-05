package com.gestion.gestion_bus.service.interfaces;

import com.gestion.gestion_bus.dto.ActivityDto;

import java.util.List;

/**
 * @author $ {USER}
 **/
public interface ActivityService {
    public List<ActivityDto> getRecentActivities(Long userId);
    public List<ActivityDto> getRecentActivitiesAdmin();
}
