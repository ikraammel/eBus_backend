package com.gestion.gestion_bus.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OccupancyDatasetDTO {
    private String label;
    private List<Double> data;
    private String color;
    private int busCount;
}