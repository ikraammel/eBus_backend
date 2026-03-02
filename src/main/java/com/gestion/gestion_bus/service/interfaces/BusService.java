package com.gestion.gestion_bus.service.interfaces;

import com.gestion.gestion_bus.dto.BusDto;

import java.util.List;

public interface BusService {
    BusDto createBus(BusDto dto);
    List<BusDto> getAllBus();
    BusDto findById(Long id);
    List<BusDto> getByLigne(Long ligneId);
    void delete(Long id);
    BusDto updateBus(Long id,BusDto dto);
    BusDto findByImmatriculation(String immatriculation);
}
