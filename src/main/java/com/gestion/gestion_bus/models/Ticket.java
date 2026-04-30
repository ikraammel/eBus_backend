package com.gestion.gestion_bus.models;

import com.gestion.gestion_bus.enums.TypeTicket;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private double prix;

    @Enumerated(EnumType.STRING)
    private TypeTicket typeTicket;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}