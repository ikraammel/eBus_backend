package com.gestion.gestion_bus.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserRequestDto {
    @NotBlank(message = "Le nom est obligatoire")
    private String nom;

    @NotBlank(message = "Le prénom est obligatoire")
    private String prenom;

    @NotBlank(message = "La date de naissance est obligatoire")
    private String dateNaissance;

    @NotBlank(message = "Le téléphone est obligatoire")
    private String tel;

    @Email(message = "Email invalide")
    @NotBlank(message = "L'email est obligatoire")
    private String email;

    @NotBlank(message = "L'adresse est obligatoire")
    private String adresse;

    @NotBlank(message = "Le CIN est obligatoire")
    private String CIN;

    @NotBlank(message = "Le CNE est obligatoire")
    private String CNE;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    @Pattern(
            regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$",
            message = "Le mot de passe doit contenir au moins une lettre et un chiffre"
    )
    private String password;

    private Long roleId;

    @NotBlank(message = "Le type d'abonnement est obligatoire")
    private String abonnement;

    private MultipartFile photo;
    private MultipartFile carteScolaire;
    private MultipartFile cin;
}