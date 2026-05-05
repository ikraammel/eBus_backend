package com.gestion.gestion_bus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DashboardStatsDto {
    private long users;
    private long bus;
    private long reclamations;
    private long objets;
}

