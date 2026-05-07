package com.gestion.gestion_bus.dto;

import com.gestion.gestion_bus.enums.StatusAbonnement;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class AbonnementDto {
    private Long id;
    private String typeNom;
    private double prix;
    private LocalDateTime dateDebut;
    private LocalDateTime dateFin;
    private StatusAbonnement status;
	private String stripeSubscriptionId;

    private Long userId;
    private String nom;
    private String prenom;
    private String email;
}
