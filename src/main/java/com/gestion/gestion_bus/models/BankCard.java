package com.gestion.gestion_bus.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "bank_cards")
public class BankCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String last4;

    private String holder;


    private String expiry;


    private int colorIndex;

    private boolean isMain;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
