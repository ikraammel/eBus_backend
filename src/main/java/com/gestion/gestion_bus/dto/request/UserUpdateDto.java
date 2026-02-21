package com.gestion.gestion_bus.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserUpdateDto {
    String nom;
    String prenom;
    String email;
    String adresse;
    String dateNaissance;
    String tel;
}
