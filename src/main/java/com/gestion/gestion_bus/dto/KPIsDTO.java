package com.gestion.gestion_bus.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KPIsDTO {
    private KPIItemDTO occupationRate;
    private KPIItemDTO punctualityRate;
    private KPIItemDTO dailyKilometers;
    private KPIItemDTO availabilityRate;
    private KPIItemDTO satisfactionScore;
}