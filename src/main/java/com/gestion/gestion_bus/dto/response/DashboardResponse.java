package com.gestion.gestion_bus.dto.response;


import com.gestion.gestion_bus.dto.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private Instant timestamp;
    private int refreshInterval;
    private long users;
    private long bus;
    private long reclamations;
    private long objets;
    private KPIsDTO kpis;
    private RealtimeOccupancyDTO realtimeOccupancy;
    private List<AlertDTO> alerts;
    private List<TopLineDTO> topLines;
    private List<EnergyConsumptionDTO> energyConsumption;
}