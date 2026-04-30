package com.gestion.gestion_bus.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TopLineDTO {
    private String line;
    private int riders;
    private double occupancy;
}