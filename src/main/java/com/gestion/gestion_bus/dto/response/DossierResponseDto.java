package com.gestion.gestion_bus.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DossierResponseDto {
    private Long id;
    private Long userId;

    private String nom;
    private String prenom;
    private String email;
    private String tel;
    private String adresse;

    private String cin;
    private String cne;

    private String photoUrl;
    private String cinUrl;
    private String carteScolaireUrl;
    private String attestationScolaireUrl;

    private String typeAbonnement;
    private double prix;
    private int dureeMois;

    private String statusDossier;
    private String rejectionReason;

    private LocalDateTime dateDebutAbonnement;
    private LocalDateTime dateFinAbonnement;
    private String statusAbonnement;
}