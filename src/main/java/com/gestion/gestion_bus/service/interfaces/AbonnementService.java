package com.gestion.gestion_bus.service.interfaces;

import com.gestion.gestion_bus.dto.AbonnementDto;
import com.gestion.gestion_bus.enums.StatusAbonnement;

import java.util.List;

public interface AbonnementService {

    String createAbonnement(Long userId, Long typeId) throws Exception;

    AbonnementDto updateStatus(Long id, StatusAbonnement status);


    AbonnementDto getAbonnementByUser(Long userId);

    AbonnementDto getAbonnementById(Long id);

    List<AbonnementDto> getAllAbonnements();


    List<AbonnementDto> getHistoriqueByUser(Long userId);

    void deleteAbonnement(Long id);
}
