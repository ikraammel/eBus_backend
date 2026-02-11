package com.gestion.gestion_bus.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String nom;

    private String prenom;

    private String tel;

    @NonNull
    @Column(unique = true)
    private String email;

    private String adresse;

    private String CIN;

    private String CNE;

    private String dateNaissance;
    private String photoUrl;
    private String carteScolaireUrl;
    private String cinUrl;

    @NonNull
    private String password;

    @ManyToOne
    @JoinColumn(name = "role_id")
    private Role role;

    @OneToMany(mappedBy = "user")
    private List<Ticket> tickets;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Abonnement abonnement;

    @OneToMany(mappedBy = "user")
    private List<Reclamation> reclamations;

    @OneToMany(mappedBy = "user")
    private List<ObjetPerdu> objetsPerdus;
}
