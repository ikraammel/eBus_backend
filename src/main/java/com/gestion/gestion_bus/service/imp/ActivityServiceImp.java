package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.ActivityDto;
import com.gestion.gestion_bus.models.Abonnement;
import com.gestion.gestion_bus.models.ObjetPerdu;
import com.gestion.gestion_bus.models.Reclamation;
import com.gestion.gestion_bus.repository.AbonnementRepository;
import com.gestion.gestion_bus.repository.ObjetPerduRepository;
import com.gestion.gestion_bus.repository.ReclamationRepository;
import com.gestion.gestion_bus.service.interfaces.ActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
public class ActivityServiceImp implements ActivityService {
    private final ReclamationRepository reclamationRepository;
    private final ObjetPerduRepository objetPerduRepository;
    private final AbonnementRepository abonnementRepository;

    @Override
    public List<ActivityDto> getRecentActivities(Long userId) {
        List<ActivityDto> activities = new ArrayList<>();

        //réclamations
        List<Reclamation> reclamations = reclamationRepository.findTop5ByUserIdOrderByDateDesc(userId);
        reclamations.forEach(r -> {
            activities.add(new ActivityDto(
                    "Reclamation",
                    "Réclamation envoyée",
                    r.getDate(),
                    r.getUser().getId(),
                    r.getUser().getNom(),
                    r.getUser().getPrenom()
            ));
        });

        //objets perdus
        List<ObjetPerdu> objetPerdus = objetPerduRepository.findTop5ByUserIdOrderByDateDeclarationDesc(userId);
        objetPerdus.forEach(o -> {
            activities.add(new ActivityDto(
                    "Objet Perdu",
                    "Objet perdu envoyé",
                    o.getDateDeclaration(),
                    o.getUser().getId(),
                    o.getUser().getNom(),
                    o.getUser().getPrenom()
            ));
        });

        // abonnements
        List<Abonnement> abonnements =
                abonnementRepository.findTop5ByUserIdOrderByDateDebutDesc(userId);

        abonnements.forEach(a -> {
            activities.add(new ActivityDto(
                    "Abonnement",
                    "Abonnement " + a.getTypeAbonnement().getNom() + " " + a.getStatus(),
                    a.getDateDebut(),
                    a.getUser().getId(),
                    a.getUser().getNom(),
                    a.getUser().getPrenom()
            ));
        });

        activities.sort((a,b) -> b.getDate().compareTo(a.getDate()));
        return activities;
    }

    @Override
    public List<ActivityDto> getRecentActivitiesAdmin() {
        List<ActivityDto> activities = new ArrayList<>();
        reclamationRepository.findTop20ByOrderByDateDesc()
                .forEach(r -> activities.add(
                        new ActivityDto(
                                "Reclamation",
                                "Réclamation envoyée",
                                r.getDate(),
                                r.getUser().getId(),
                                r.getUser().getNom(),
                                r.getUser().getPrenom()
                        )
                ));
        objetPerduRepository.findTop20ByOrderByDateDeclarationDesc()
                        .forEach(obj -> activities.add(
                                new ActivityDto(
                                        "Objet Perdu",
                                        "Objet perdu envoyé",
                                        obj.getDateDeclaration(),
                                        obj.getUser().getId(),
                                        obj.getUser().getNom(),
                                        obj.getUser().getPrenom()
                                )
                        ));
        abonnementRepository.findTop20ByOrderByDateDebutDesc()
                        .forEach(a -> activities.add(new ActivityDto(
                                "Abonnement",
                                "Abonnement " + a.getTypeAbonnement().getNom() + " " + a.getStatus(),
                                a.getDateDebut(),
                                a.getUser().getId(),
                                a.getUser().getNom(),
                                a.getUser().getPrenom()
                        )
                        ));

        activities.sort((a,b) -> b.getDate().compareTo(a.getDate()));
        return activities.stream().limit(50).toList();
    }
}
