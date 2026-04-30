package com.gestion.gestion_bus.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EnergyConsumptionDTO {
    private String type;
    private double percentage;
    private String color;
}