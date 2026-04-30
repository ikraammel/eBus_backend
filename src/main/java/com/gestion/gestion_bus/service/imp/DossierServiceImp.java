package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.response.DossierResponseDto;
import com.gestion.gestion_bus.enums.StatusAbonnement;
import com.gestion.gestion_bus.enums.StatusDossier;
import com.gestion.gestion_bus.mappers.DossierMapper;
import com.gestion.gestion_bus.models.Abonnement;
import com.gestion.gestion_bus.models.Dossier;
import com.gestion.gestion_bus.repository.AbonnementRepository;
import com.gestion.gestion_bus.repository.DossierRepository;
import com.gestion.gestion_bus.service.interfaces.DossierService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DossierServiceImp implements DossierService {

    private final DossierRepository dossierRepository;
    private final DossierMapper dossierMapper;
    private final AbonnementRepository abonnementRepository;

    @Override
    public List<DossierResponseDto> getAllDossiers() {
        return dossierRepository.findAll()
                .stream()
                .map(dossier -> {
                    Abonnement ab = dossier.getUser().getAbonnements()
                            .stream()
                            .max(Comparator.comparing(Abonnement::getDateDebut))
                            .orElse(null);

                    return dossierMapper.toDto(dossier, ab);
                })
                .toList();
    }

    @Override
    public List<DossierResponseDto> getDossiersEnAttente() {
        return dossierRepository.findByStatus(StatusDossier.EN_ATTENTE)
                .stream()
                .map(dossier -> {
                    Abonnement ab = dossier.getUser().getAbonnements()
                            .stream()
                            .max(Comparator.comparing(Abonnement::getDateDebut))
                            .orElse(null);

                    return dossierMapper.toDto(dossier, ab);
                })
                .toList();
    }

    @Override
    public DossierResponseDto getMyDossier(Long userId) {

        Dossier dossier = dossierRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Dossier introuvable"));

        Abonnement ab = dossier.getUser().getAbonnements()
                .stream()
                .max(Comparator.comparing(Abonnement::getDateDebut))
                .orElse(null);

        return dossierMapper.toDto(dossier, ab);
    }

    @Override
    public DossierResponseDto getDossier(Long id) {
        Dossier dossier = dossierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Dossier introuvable"));

        Abonnement ab = dossier.getUser().getAbonnements()
                .stream()
                .max(Comparator.comparing(Abonnement::getDateDebut))
                .orElse(null);

        return dossierMapper.toDto(dossier, ab);
    }

    @Override
    public void validerDossier(Long id) {
        Dossier dossier = dossierRepository.findById(id).orElseThrow();
        dossier.setStatus(StatusDossier.VALIDE);
        dossier.setUpdatedAt(LocalDateTime.now());
        Abonnement abo = abonnementRepository.findByUserId(dossier.getUser().getId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (abo != null) {
            abo.setStatus(StatusAbonnement.ACTIF);
            abonnementRepository.save(abo);
        }
        dossierRepository.save(dossier);
    }

    @Override
    public void rejeterDossier(Long id) {
        Dossier dossier = dossierRepository.findById(id).orElseThrow();
        dossier.setStatus(StatusDossier.REJETE);
        dossier.setUpdatedAt(LocalDateTime.now());
        dossierRepository.save(dossier);
    }
}