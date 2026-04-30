package com.gestion.gestion_bus.dto.response;

import lombok.Data;

import java.time.LocalDate;

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

    private String typeAbonnement;
    private double prix;
    private int dureeMois;

    private String statusDossier;

    private LocalDate dateDebutAbonnement;
    private LocalDate dateFinAbonnement;
    private String statusAbonnement;
}