package com.gestion.gestion_bus.models;

import com.gestion.gestion_bus.enums.Status;
import com.gestion.gestion_bus.enums.TypeAbonnement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Abonnement {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private TypeAbonnement type;

    private double prix;
    private LocalDate dateDebut;
    private LocalDate dateFin;

    private boolean actif;

    private Status status;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToOne(mappedBy = "abonnement")
    private Payment payment;
}
