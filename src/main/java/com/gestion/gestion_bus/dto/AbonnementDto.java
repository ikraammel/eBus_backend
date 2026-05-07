package com.gestion.gestion_bus.dto;

import com.gestion.gestion_bus.enums.StatusAbonnement;
import lombok.Data;

import java.time.LocalDate;

@Data
public class AbonnementDto {
    private Long id;
    private String typeNom;
    private double prix;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private StatusAbonnement status;
	private String stripeSubscriptionId;

    private Long userId;
    private String nom;
    private String prenom;
    private String email;
}
