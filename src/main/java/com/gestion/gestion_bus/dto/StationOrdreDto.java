package com.gestion.gestion_bus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StationOrdreDto {
    private String nom;
    private int ordre;
    private String direction; // "ALLER" ou "RETOUR"
}