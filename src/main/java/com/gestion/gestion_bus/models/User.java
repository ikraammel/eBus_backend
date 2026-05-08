package com.gestion.gestion_bus.models;

import com.gestion.gestion_bus.enums.StatusDossier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
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

    private String cin;

    private String cne;

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

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Abonnement> abonnements = new ArrayList<>();

    @OneToMany(mappedBy = "user")
    private List<Reclamation> reclamations;

    @Enumerated(EnumType.STRING)
    private StatusDossier statusDossier = StatusDossier.EN_ATTENTE;

    @OneToMany(mappedBy = "user")
    private List<ObjetPerdu> objetsPerdus;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
}
