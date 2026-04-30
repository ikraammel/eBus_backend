package com.gestion.gestion_bus.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KPIItemDTO {
    private double value;
    private String trend;
    private String status;
    private Double threshold;
    private Double thresholdMin;
    private Double thresholdMax;
    private String unit;
}