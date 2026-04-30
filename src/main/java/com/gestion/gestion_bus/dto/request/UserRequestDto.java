package com.gestion.gestion_bus.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;
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
    private String cin;

    private String cne;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8)
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d).+$")
    private String password;

    private Long roleId;

    @NotNull(message = "L'abonnement est obligatoire")
    private Long abonnementId;

}