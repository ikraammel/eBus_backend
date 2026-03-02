package com.gestion.gestion_bus.dto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LigneDto {
    private Long id;
    private String numero;
    private double distance;
    private String startPoint;
    private String endPoint;
    private List<StationOrdreDto> stations;

}
