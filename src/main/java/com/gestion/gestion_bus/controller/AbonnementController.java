package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.dto.AbonnementDto;
import com.gestion.gestion_bus.enums.StatusAbonnement;
import com.gestion.gestion_bus.models.Abonnement;
import com.gestion.gestion_bus.models.Payment;
import com.gestion.gestion_bus.models.TypeAbonnement;
import com.gestion.gestion_bus.models.User;
import com.gestion.gestion_bus.repository.AbonnementRepository;
import com.gestion.gestion_bus.repository.PaymentRepository;
import com.gestion.gestion_bus.repository.TypeAbonnementRepository;
import com.gestion.gestion_bus.repository.UserRepository;
import com.gestion.gestion_bus.service.imp.StripeService;
import com.gestion.gestion_bus.service.interfaces.AbonnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import com.stripe.model.checkout.Session;

@RestController
@RequestMapping("/abonnements")
@RequiredArgsConstructor
public class AbonnementController {

    private final AbonnementService service;
    private final AbonnementRepository abonnementRepository;
    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;
    private final UserRepository userRepository;
    private final TypeAbonnementRepository typeAbonnementRepository;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/subscribe")
    public ResponseEntity<String> create(
            @RequestParam Long userId,
            @RequestParam Long typeId) {
        try {
            String url = service.createAbonnement(userId, typeId);
            return ResponseEntity.ok(url);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/confirm")
    @Transactional
    public ResponseEntity<String> confirmPayment(
            @RequestParam String sessionId) {

        try {

            Session session = stripeService.retrieveSession(sessionId);

            boolean paid =
                    "paid".equals(session.getPaymentStatus())
                            || "complete".equals(session.getStatus());

            if (!paid) {
                return ResponseEntity.ok("EN_ATTENTE");
            }

            Long userId =
                    Long.valueOf(session.getMetadata().get("userId"));

            Long typeAbonnementId =
                    Long.valueOf(session.getMetadata().get("typeAbonnementId"));

            Optional<Abonnement> existing =
                    abonnementRepository
                            .findTopByUserIdAndStatusOrderByIdDesc(
                                    userId,
                                    StatusAbonnement.ACTIF
                            );

            if (existing.isPresent()) {
                return ResponseEntity.ok("ACTIF");
            }

            User user = userRepository.findById(userId)
                    .orElseThrow();

            TypeAbonnement type =
                    typeAbonnementRepository.findById(typeAbonnementId)
                            .orElseThrow();

            Abonnement abonnement = new Abonnement();

            abonnement.setUser(user);
            abonnement.setTypeAbonnement(type);
            abonnement.setPrix(type.getPrix());
            abonnement.setDateDebut(LocalDateTime.now());
            abonnement.setDateFin(
                    LocalDateTime.now()
                            .plusMonths(type.getDureeMois())
            );
            abonnement.setStatus(StatusAbonnement.ACTIF);

            abonnement.setStripeSubscriptionId(
                    session.getSubscription()
            );

            Abonnement saved =
                    abonnementRepository.save(abonnement);

            Payment payment = new Payment();

            payment.setUser(user);
            payment.setAbonnement(saved);
            payment.setMontant(type.getPrix());
            payment.setStatut("PAYE");
            payment.setDatePaiement(LocalDateTime.now());

            paymentRepository.save(payment);

            return ResponseEntity.ok("ACTIF");

        } catch (Exception e) {

            e.printStackTrace();

            return ResponseEntity.badRequest()
                    .body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<AbonnementDto> getAll() {
        return service.getAllAbonnements();
    }

    @PreAuthorize("hasRole('USER') or #userId == authentication.principal.id")
    @GetMapping("/user/{userId}")
    public ResponseEntity<AbonnementDto> getByUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(service.getAbonnementByUser(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.noContent().build();
        }
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/user/{userId}/historique")
    public List<AbonnementDto> getHistorique(@PathVariable Long userId) {
        return service.getHistoriqueByUser(userId);
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping("/{id}/status")
    public StatusAbonnement getStatus(@PathVariable Long id) {
        return service.getAbonnementById(id).getStatus();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteAbonnement(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/status")
    public AbonnementDto updateStatus(
            @PathVariable Long id,
            @RequestParam StatusAbonnement status) {
        return service.updateStatus(id, status);
    }
}
