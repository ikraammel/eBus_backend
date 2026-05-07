package com.gestion.gestion_bus.models;

import com.gestion.gestion_bus.enums.StatusAbonnement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Abonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private TypeAbonnement typeAbonnement;
	private String stripeSubscriptionId;

    private double prix;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    @Enumerated(EnumType.STRING)
    private StatusAbonnement status;
    @ManyToOne
    private User user;
}
