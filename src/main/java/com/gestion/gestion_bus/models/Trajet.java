package com.gestion.gestion_bus.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "trajets")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trajet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bus_id")
    private Bus bus;

    private String ligne;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private int passengersCount;
    private double distanceKm;
    private boolean onTime;

    @Enumerated(EnumType.STRING)
    private TrajetStatus status;

    public enum TrajetStatus {
        PLANIFIE, EN_COURS, TERMINE, ANNULE
    }
}