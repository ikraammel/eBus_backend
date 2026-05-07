package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.enums.StatusAbonnement;
import com.gestion.gestion_bus.models.Abonnement;
import com.gestion.gestion_bus.models.Payment;
import com.gestion.gestion_bus.repository.AbonnementRepository;
import com.gestion.gestion_bus.repository.PaymentRepository;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/stripe")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final AbonnementRepository abonnementRepository;
    private final PaymentRepository paymentRepository;


    @Value("${stripe.webhook.secret}")
    private String endpointSecret;

    @PostMapping("/webhook")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        System.out.println("[Webhook] Réception événement Stripe...");

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, endpointSecret);
            System.out.println("[Webhook] Type : " + event.getType());

            if ("checkout.session.completed".equals(event.getType())) {
                EventDataObjectDeserializer des = event.getDataObjectDeserializer();
                if (des.getObject().isEmpty()) {
                    System.out.println("[Webhook] ❌ Impossible de désérialiser la session");
                    return ResponseEntity.ok("SKIP");
                }

                Session session = (Session) des.getObject().get();
                String abonnementIdStr = session.getMetadata().get("abonnementId");

                if (abonnementIdStr == null) {
                    System.out.println("[Webhook] ⚠️ Metadata 'abonnementId' absente");
                    return ResponseEntity.ok("SKIP");
                }

                Long aboId = Long.valueOf(abonnementIdStr);
                activateAbonnement(aboId, session.getSubscription());
            }

        } catch (Exception e) {
            System.out.println("[Webhook] ❌ Erreur : " + e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        return ResponseEntity.ok("OK");
    }

    private void activateAbonnement(Long aboId, String stripeSubscriptionId) {
        Optional<Abonnement> opt = abonnementRepository.findById(aboId);
        if (opt.isEmpty()) {
            System.out.println("[Webhook] ❌ Abonnement " + aboId + " introuvable");
            return;
        }

        Abonnement abo = opt.get();
        abo.setStatus(StatusAbonnement.ACTIF);
        if (stripeSubscriptionId != null) {
            abo.setStripeSubscriptionId(stripeSubscriptionId);
        }
        abonnementRepository.save(abo);
        System.out.println("[Webhook] ✅ Abonnement " + aboId + " → ACTIF");

        paymentRepository.findByAbonnementId(aboId).ifPresent(p -> {
            p.setStatut("PAYE");
            p.setDatePaiement(LocalDateTime.now());
            paymentRepository.save(p);
            System.out.println("[Webhook] ✅ Payment → PAYE");
        });
    }
}
