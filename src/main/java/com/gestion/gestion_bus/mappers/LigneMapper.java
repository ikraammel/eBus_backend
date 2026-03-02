package com.gestion.gestion_bus.mappers;

import com.gestion.gestion_bus.dto.LigneDto;
import com.gestion.gestion_bus.dto.StationOrdreDto;
import com.gestion.gestion_bus.models.Ligne;

import java.util.stream.Collectors;

public class LigneMapper {
    public static LigneDto toDto(Ligne ligne){
        return new LigneDto(
                ligne.getId(),
                ligne.getNumero(),
                ligne.getDistance(),
                ligne.getStartPoint(),
                ligne.getEndPoint(),
                ligne.getTrajets()
                        .stream()
                        .map(ls -> new StationOrdreDto(
                                ls.getStation().getNom(),
                                ls.getOrdre(),
                                ls.getDirection()
                        ))
                        .collect(Collectors.toList())
        );
    }

}
