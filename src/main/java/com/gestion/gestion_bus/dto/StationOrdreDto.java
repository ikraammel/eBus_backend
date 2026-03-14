package com.gestion.gestion_bus.dto;

import com.gestion.gestion_bus.models.Position;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StationOrdreDto {
    private Long id;
    private String nom;
    private int ordre;
    private String direction; // "ALLER" ou "RETOUR"
    private Position position;
}