package com.gestion.gestion_bus.service.interfaces;

import com.gestion.gestion_bus.dto.LigneDto;
import com.gestion.gestion_bus.models.Ligne;

import java.util.List;

public interface LigneService {
    LigneDto createLine(LigneDto ligneDto);
    List<LigneDto> getAllLines();
    LigneDto getLineById(Long id);
    LigneDto getLineByNumero(String numero);
    void deleteLine(Long id);
    LigneDto updateLine(Long id,LigneDto updatedLine);
}
