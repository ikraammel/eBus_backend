package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.AbonnementDto;
import com.gestion.gestion_bus.enums.StatusAbonnement;
import com.gestion.gestion_bus.mappers.AbonnementMapper;
import com.gestion.gestion_bus.models.Abonnement;
import com.gestion.gestion_bus.models.Payment;
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

@Service
@RequiredArgsConstructor
public class AbonnementServiceImp implements AbonnementService {

    private final AbonnementRepository abonnementRepository;
    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final TypeAbonnementRepository typeAbonnementRepository;
    private final StripeService stripeService;

    /** Calcule le statut effectif : si dateFin est passée on retourne EXPIRE dans le DTO */
    public AbonnementDto mapWithEffectiveStatus(Abonnement a) {
        AbonnementDto dto = AbonnementMapper.mapToDto(a);
        if (a.getStatus() == StatusAbonnement.ACTIF
                && a.getDateFin() != null
                && a.getDateFin().isBefore(LocalDate.now())) {
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

        if (!type.isActif())
            throw new RuntimeException("Ce type d'abonnement est désactivé");

        // ── RÈGLE : un seul abonnement ACTIF à la fois ───────────────────────
        Optional<Abonnement> existingActif = abonnementRepository
            .findTopByUserIdAndStatusOrderByIdDesc(userId, StatusAbonnement.ACTIF);
        if (existingActif.isPresent()) {
            Abonnement a = existingActif.get();
            // S'il n'est pas encore expiré, on refuse
            if (a.getDateFin() != null && !a.getDateFin().isBefore(LocalDate.now())) {
                throw new RuntimeException("Vous avez déjà un abonnement actif jusqu'au " + a.getDateFin());
            }
            // Sinon on le marque EXPIRE en base
            a.setStatus(StatusAbonnement.EXPIRE);
            abonnementRepository.save(a);
        }

        // Annuler les éventuels abonnements EN_ATTENTE antérieurs
        Optional<Abonnement> existingPending = abonnementRepository
            .findTopByUserIdAndStatusOrderByIdDesc(userId, StatusAbonnement.EN_ATTENTE);
        existingPending.ifPresent(a -> {
            a.setStatus(StatusAbonnement.REFUSE);
            abonnementRepository.save(a);
        });

        // ── Création du nouvel abonnement EN_ATTENTE ─────────────────────────
        Abonnement a = new Abonnement();
        a.setUser(user);
        a.setTypeAbonnement(type);
        a.setPrix(type.getPrix());
        a.setDateDebut(LocalDate.now());
        a.setDateFin(LocalDate.now().plusMonths(type.getDureeMois()));
        a.setStatus(StatusAbonnement.EN_ATTENTE);
        Abonnement saved = abonnementRepository.save(a);

        // ── Création du Payment EN_ATTENTE lié ───────────────────────────────
        Payment payment = new Payment();
        payment.setMontant(type.getPrix());
        payment.setStatut("EN_ATTENTE");
        payment.setDatePaiement(LocalDate.now());
        payment.setUser(user);
        payment.setAbonnement(saved);
        paymentRepository.save(payment);

        // ── Génération de la session Stripe ──────────────────────────────────
        return stripeService.createCheckoutSession(
            type.getStripePriceId(),
            saved.getId(),
            user.getEmail()
        );
    }

    @Override
    @Transactional
    public AbonnementDto updateStatus(Long id, StatusAbonnement status) {
        Abonnement a = abonnementRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Abonnement introuvable"));
        a.setStatus(status);
        abonnementRepository.save(a);

        // Mettre à jour aussi le Payment lié
        paymentRepository.findByAbonnementId(id).ifPresent(p -> {
            p.setStatut(status == StatusAbonnement.ACTIF ? "PAYE" : status.name());
            paymentRepository.save(p);
        });

        return mapWithEffectiveStatus(a);
    }

    @Override
    public AbonnementDto getAbonnementByUser(Long userId) {
        // Retourne le plus récent (actif, en attente ou refusé)
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
