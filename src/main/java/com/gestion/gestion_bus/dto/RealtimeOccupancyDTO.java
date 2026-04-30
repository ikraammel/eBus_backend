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
public class RealtimeOccupancyDTO {
    private List<String> labels;
    private List<OccupancyDatasetDTO> datasets;
}