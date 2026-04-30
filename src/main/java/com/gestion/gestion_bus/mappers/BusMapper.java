package com.gestion.gestion_bus.mappers;

import com.gestion.gestion_bus.dto.BusDto;
import com.gestion.gestion_bus.models.Bus;
import org.springframework.stereotype.Component;

@Component
public class BusMapper {
    public static BusDto toDto(Bus bus) {
        if (bus == null) {
            return null;
        }
        return new BusDto(
                bus.getId(),
                bus.getNumero(),
                bus.getEtat() != null ? bus.getEtat().toString() : null,
                bus.getImmatriculation(),
                bus.getLigne() != null ? bus.getLigne().getId() : null
        );
    }
}