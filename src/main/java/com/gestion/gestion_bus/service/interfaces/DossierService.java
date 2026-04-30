package com.gestion.gestion_bus.service.interfaces;


import com.gestion.gestion_bus.dto.response.DossierResponseDto;

import java.util.List;

public interface DossierService {
    List<DossierResponseDto> getDossiersEnAttente();
    DossierResponseDto getDossier(Long id);
    void validerDossier(Long id);
    void rejeterDossier(Long id);
    DossierResponseDto getMyDossier(Long userId);
    List<DossierResponseDto> getAllDossiers();
}
