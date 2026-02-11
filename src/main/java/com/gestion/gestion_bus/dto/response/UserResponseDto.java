package com.gestion.gestion_bus.dto.response;

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
}
