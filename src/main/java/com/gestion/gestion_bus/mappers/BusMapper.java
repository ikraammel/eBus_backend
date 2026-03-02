package com.gestion.gestion_bus.mappers;

import com.gestion.gestion_bus.dto.BusDto;
import com.gestion.gestion_bus.models.Bus;

public class BusMapper {
    public static BusDto toDto(Bus bus){
        return new BusDto(
           bus.getId(),
           bus.getEtat(),
           bus.getImmatriculation(),
           bus.getLigne().getId()
        );
    }
}
