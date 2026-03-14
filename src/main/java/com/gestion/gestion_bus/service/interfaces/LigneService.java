package com.gestion.gestion_bus.service.interfaces;

import com.gestion.gestion_bus.dto.LigneDto;
import com.gestion.gestion_bus.dto.StationOrdreDto;

import java.util.List;

public interface LigneService {
    LigneDto createLine(LigneDto ligneDto);
    List<LigneDto> getAllLines();
    LigneDto getLineById(Long id);
    LigneDto getLineByNumero(String numero);
    void deleteLine(Long id);
    LigneDto updateLine(Long id,LigneDto updatedLine);
    StationOrdreDto addStation(StationOrdreDto station,Long ligneId);
    StationOrdreDto updateStation(StationOrdreDto station,Long ligneId,Long stationId);
    void deleteStation(Long ligneId,Long stationId);
}
