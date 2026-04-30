package com.gestion.gestion_bus.service.interfaces;

import com.gestion.gestion_bus.dto.AbonnementDto;
import com.gestion.gestion_bus.enums.StatusAbonnement;

import java.util.List;

public interface AbonnementService {

    AbonnementDto createAbonnement(Long userId, Long typeId);
    AbonnementDto getAbonnementByUser(Long userId);
    List<AbonnementDto> getAllAbonnements();
    void deleteAbonnement(Long id);
    AbonnementDto updateStatus(Long id, StatusAbonnement status);
}