package com.gestion.gestion_bus.models;

import com.gestion.gestion_bus.enums.StatusAbonnement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

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
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    @Enumerated(EnumType.STRING)
    private StatusAbonnement status;
    @ManyToOne
    private User user;
}
