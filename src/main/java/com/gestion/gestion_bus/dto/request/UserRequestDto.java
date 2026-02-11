package com.gestion.gestion_bus.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {
    private String nom;
    private String prenom;
    private String dateNaissance;
    private String tel;
    private String email;
    private String adresse;
    private String CIN;
    private String CNE;
    private String password;
    private Long roleId;
    private String abonnement;

    private String attestationUrl;
    private String photoUrl;
    private String carteScolaireUrl;


}
