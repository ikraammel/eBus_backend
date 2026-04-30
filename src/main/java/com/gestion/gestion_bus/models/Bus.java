package com.gestion.gestion_bus.models;

import com.gestion.gestion_bus.enums.BusEtat;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Bus {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String numero;

    @Enumerated(EnumType.STRING)
    private BusEtat etat;

    private Integer capacity;

    private String immatriculation;

    @ManyToOne
    @JoinColumn(name = "ligne_id")
    private Ligne ligne;
}
