package com.gestion.gestion_bus.models;

import com.gestion.gestion_bus.enums.StatusDossier;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dossier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    private StatusDossier status = StatusDossier.EN_ATTENTE;

    private String commentaireAdmin;

    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt;
}