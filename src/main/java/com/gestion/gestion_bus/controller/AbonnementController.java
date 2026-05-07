package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.dto.AbonnementDto;
import com.gestion.gestion_bus.enums.StatusAbonnement;
import com.gestion.gestion_bus.models.Abonnement;
import com.gestion.gestion_bus.models.Payment;
import com.gestion.gestion_bus.repository.AbonnementRepository;
import com.gestion.gestion_bus.repository.PaymentRepository;
import com.gestion.gestion_bus.service.imp.StripeService;
import com.gestion.gestion_bus.service.interfaces.AbonnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/abonnements")
@RequiredArgsConstructor
public class AbonnementController {

    private final AbonnementService service;
    private final AbonnementRepository abonnementRepository;
    private final PaymentRepository paymentRepository;
    private final StripeService stripeService;


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


    @PostMapping("/{id}/confirm")
    public ResponseEntity<String> confirmPayment(
            @PathVariable Long id,
            @RequestParam String sessionId) {

        System.out.println("[Confirm] Vérification paiement pour abonnement=" + id + " session=" + sessionId);

        // 1. Vérifier que l'abonnement existe
        Optional<Abonnement> opt = abonnementRepository.findById(id);
        if (opt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Abonnement abo = opt.get();

        // 2. Si déjà ACTIF → ne rien faire (idempotent)
        if (abo.getStatus() == StatusAbonnement.ACTIF) {
            System.out.println("[Confirm] Abonnement déjà ACTIF");
            return ResponseEntity.ok("ACTIF");
        }

        // 3. Demander à Stripe si la session est bien payée
        boolean paid = stripeService.isSessionPaid(sessionId);
        System.out.println("[Confirm] Stripe payé = " + paid);

        if (paid) {
            // 4. Activer l'abonnement
            abo.setStatus(StatusAbonnement.ACTIF);
            abonnementRepository.save(abo);

            // 5. Mettre à jour le Payment lié
            paymentRepository.findByAbonnementId(id).ifPresent(p -> {
                p.setStatut("PAYE");
                p.setDatePaiement(LocalDate.now());
                paymentRepository.save(p);
            });

            System.out.println("[Confirm] ✅ Abonnement " + id + " activé");
            return ResponseEntity.ok("ACTIF");
        } else {
            System.out.println("[Confirm] ⏳ Paiement pas encore confirmé");
            return ResponseEntity.ok("EN_ATTENTE");
        }
    }

    /** Liste tous les abonnements (admin). */
    @GetMapping
    public List<AbonnementDto> getAll() {
        return service.getAllAbonnements();
    }


    @GetMapping("/user/{userId}")
    public ResponseEntity<AbonnementDto> getByUser(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(service.getAbonnementByUser(userId));
        } catch (RuntimeException e) {
            return ResponseEntity.noContent().build();
        }
    }


    @GetMapping("/user/{userId}/historique")
    public List<AbonnementDto> getHistorique(@PathVariable Long userId) {
        return service.getHistoriqueByUser(userId);
    }


    @GetMapping("/{id}/status")
    public StatusAbonnement getStatus(@PathVariable Long id) {
        return service.getAbonnementById(id).getStatus();
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteAbonnement(id);
    }

    @PatchMapping("/{id}/status")
    public AbonnementDto updateStatus(
            @PathVariable Long id,
            @RequestParam StatusAbonnement status) {
        return service.updateStatus(id, status);
    }
}
