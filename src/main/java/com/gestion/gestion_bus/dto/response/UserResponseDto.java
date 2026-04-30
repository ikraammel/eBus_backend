package com.gestion.gestion_bus.dto.response;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String nom;
    private String prenom;
    private String email;
    private String tel;
    private String adresse;
    private String role;
    private String dateNaissance;
    private String typeAbonnement;
    private String statutAbonnement;
    private String photoUrl;
    private String carteScolaireUrl;
    private String cinUrl;
    private String cin;
    private String cne;
}
