package com.gestion.gestion_bus.models;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "alerts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alert {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String alertId;

    @ManyToOne
    @JoinColumn(name = "bus_id")
    private Bus bus;

    @Enumerated(EnumType.STRING)
    private Severity severity;

    private String message;
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    private AlertStatus status;

    public enum Severity {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum AlertStatus {
        ACTIVE, ACKNOWLEDGED, RESOLVED
    }
}