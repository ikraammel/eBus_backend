package com.gestion.gestion_bus.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double montant;
    private String statut; // PAYE, EN_ATTENTE
    private LocalDate datePaiement;

    @ManyToOne
    private User user;

    @OneToOne
    private Ticket ticket;

    @OneToOne
    private Abonnement abonnement;
}
