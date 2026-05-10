package com.gestion.gestion_bus.models;

import com.gestion.gestion_bus.enums.StatusDossier;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User implements UserDetails {
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

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return role.getAuthorities();
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
