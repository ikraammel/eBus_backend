package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.AbonnementDto;
import com.gestion.gestion_bus.enums.StatusAbonnement;
import com.gestion.gestion_bus.mappers.AbonnementMapper;
import com.gestion.gestion_bus.models.Abonnement;
import com.gestion.gestion_bus.models.TypeAbonnement;
import com.gestion.gestion_bus.models.User;
import com.gestion.gestion_bus.repository.AbonnementRepository;
import com.gestion.gestion_bus.repository.PaymentRepository;
import com.gestion.gestion_bus.repository.TypeAbonnementRepository;
import com.gestion.gestion_bus.repository.UserRepository;
import com.gestion.gestion_bus.service.interfaces.AbonnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import com.gestion.gestion_bus.enums.StatusDossier;


@Service
@RequiredArgsConstructor
public class AbonnementServiceImp implements AbonnementService {

    private final AbonnementRepository abonnementRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final TypeAbonnementRepository typeAbonnementRepository;
    private final StripeService stripeService;


    public AbonnementDto mapWithEffectiveStatus(Abonnement a) {
        AbonnementDto dto = AbonnementMapper.mapToDto(a);
        if (a.getStatus() == StatusAbonnement.ACTIF
            && a.getDateFin() != null
            && a.getDateFin().toLocalDate().isBefore(LocalDate.now())) {
            dto.setStatus(StatusAbonnement.EXPIRE);
        }
        return dto;
    }

    @Override
    @Transactional
    public String createAbonnement(Long userId, Long typeId) throws Exception {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        TypeAbonnement type = typeAbonnementRepository.findById(typeId)
            .orElseThrow(() -> new RuntimeException("Type d'abonnement introuvable"));

        // ── Vérifications dossier ──────────────────────────────────────────
        if (user.getStatusDossier() == null) {
            throw new RuntimeException("Votre dossier n'est pas encore traité");
        }
        if (user.getStatusDossier() == StatusDossier.EN_ATTENTE) {
            throw new RuntimeException("Votre dossier est en attente de validation");
        }
        if (user.getStatusDossier() == StatusDossier.REJETE) {
            throw new RuntimeException(
                "Votre dossier a été rejeté. Veuillez modifier vos informations personnelles"
            );
        }
        if (user.getStatusDossier() != StatusDossier.VALIDE) {
            throw new RuntimeException("Votre dossier doit être validé avant abonnement");
        }
        if (!type.isActif()) {
            throw new RuntimeException("Type abonnement désactivé");
        }

        // ── Vérifier abonnement ACTIF ──────────────────────────────────────
        Optional<Abonnement> existingActif =
            abonnementRepository.findTopByUserIdAndStatusOrderByIdDesc(
                userId, StatusAbonnement.ACTIF
            );

        if (existingActif.isPresent()) {
            Abonnement abo = existingActif.get();
            if (abo.getDateFin() != null
                && abo.getDateFin().toLocalDate().isBefore(LocalDate.now())) {
                // Expiré → on marque EXPIRE et on continue
                abo.setStatus(StatusAbonnement.EXPIRE);
                abonnementRepository.save(abo);
            } else {
                throw new RuntimeException("Vous avez déjà un abonnement actif");
            }
        }
        return stripeService.createCheckoutSession(
                type.getStripePriceId(),
                user.getId(),
                type.getId(),
                user.getEmail()
        );

    }

    @Override
    @Transactional
    public AbonnementDto updateStatus(Long id, StatusAbonnement status) {
        Abonnement a = abonnementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Abonnement not found"));
        a.setStatus(status);
        abonnementRepository.save(a);
        paymentRepository.findByAbonnementId(id).ifPresent(p -> {
            p.setStatut(status == StatusAbonnement.ACTIF ? "PAYE" : status.name());
            paymentRepository.save(p);
        });
        return mapWithEffectiveStatus(a);
    }

    @Override
    public AbonnementDto getAbonnementByUser(Long userId) {
        Abonnement a = abonnementRepository
            .findTopByUserIdOrderByIdDesc(userId)
            .orElseThrow(() -> new RuntimeException("Aucun abonnement trouvé pour cet utilisateur"));
        return mapWithEffectiveStatus(a);
    }

    @Override
    public AbonnementDto getAbonnementById(Long id) {
        Abonnement a = abonnementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Abonnement introuvable"));
        return mapWithEffectiveStatus(a);
    }

    @Override
    public List<AbonnementDto> getAllAbonnements() {
        return abonnementRepository.findAll()
            .stream()
            .map(this::mapWithEffectiveStatus)
            .toList();
    }

    @Override
    public List<AbonnementDto> getHistoriqueByUser(Long userId) {
        return abonnementRepository.findAllByUserIdOrderByIdDesc(userId)
            .stream()
            .map(this::mapWithEffectiveStatus)
            .toList();
    }

    @Override
    public void deleteAbonnement(Long id) {
        abonnementRepository.deleteById(id);
    }
}
