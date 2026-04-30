package com.gestion.gestion_bus.mappers;

import com.gestion.gestion_bus.dto.AbonnementDto;
import com.gestion.gestion_bus.models.Abonnement;
import org.springframework.stereotype.Component;

@Component
public class AbonnementMapper {
    public static AbonnementDto mapToDto(Abonnement a) {
        AbonnementDto dto = new AbonnementDto();
        dto.setId(a.getId());
        dto.setTypeNom(
                a.getTypeAbonnement() != null
                        ? a.getTypeAbonnement().getNom()
                        : "Non défini"
        );
        dto.setPrix(a.getPrix());
        dto.setDateDebut(a.getDateDebut());
        dto.setDateFin(a.getDateFin());
        dto.setStatus(a.getStatus());
        dto.setUserId(a.getUser().getId());
        dto.setNom(a.getUser().getNom());
        dto.setPrenom(a.getUser().getPrenom());
        dto.setEmail(a.getUser().getEmail());

        return dto;
    }
}
