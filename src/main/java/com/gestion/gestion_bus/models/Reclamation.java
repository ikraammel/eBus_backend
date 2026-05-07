package com.gestion.gestion_bus.models;

import com.gestion.gestion_bus.enums.ReclamationStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Reclamation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String titre;
    private String description;
    @Enumerated(EnumType.STRING)
    private ReclamationStatus status;
    private LocalDateTime date;

    @ManyToOne
    @JoinColumn(name = "ligne_id")
    private Ligne ligne;

    @ManyToOne
    private User user;
}
