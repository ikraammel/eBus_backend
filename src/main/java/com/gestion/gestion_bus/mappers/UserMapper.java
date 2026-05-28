package com.gestion.gestion_bus.mappers;

import com.gestion.gestion_bus.dto.response.UserResponseDto;
import com.gestion.gestion_bus.models.Abonnement;
import com.gestion.gestion_bus.models.User;
import com.gestion.gestion_bus.repository.DossierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Comparator;

@RequiredArgsConstructor
@Component
public class UserMapper {
    private final DossierRepository dossierRepository;

    public UserResponseDto toDto(User user){
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());
        dto.setTel(user.getTel());
        dto.setAdresse(user.getAdresse());
        dto.setDateNaissance(user.getDateNaissance().toString());
        dto.setRole(user.getRole().getName());
        dto.setCin(user.getCin());
        dto.setCne(user.getCne());

        dto.setPhotoUrl(user.getPhotoUrl());
        dto.setCarteScolaireUrl(user.getCarteScolaireUrl());
        dto.setAttestationScolaireUrl(user.getAttestationScolaireUrl());
        dto.setCinUrl(user.getCinUrl());

        if (user.getAbonnements() != null && !user.getAbonnements().isEmpty()) {

            Abonnement last = user.getAbonnements()
                    .stream()
                    .max(Comparator.comparing(Abonnement::getDateDebut))
                    .orElse(null);

            if (last != null) {
                dto.setTypeAbonnement(last.getTypeAbonnement().getNom()); // ETUDIANT_ANNUEL
                dto.setStatutAbonnement(last.getStatus().name());         // EN_ATTENTE
            } else {
                dto.setTypeAbonnement(null);
                dto.setStatutAbonnement(null);
            }

        } else {
            dto.setTypeAbonnement(null);
            dto.setStatutAbonnement(null);
        }

        dossierRepository.findByUserId(user.getId()).ifPresent(dossier -> {
            dto.setStatusDossier(dossier.getStatus().name());
            dto.setRejectionReason(dossier.getRejectionReason());
        });
        return dto;
    }
}
