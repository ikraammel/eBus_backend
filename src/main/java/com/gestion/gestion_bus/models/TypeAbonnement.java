package com.gestion.gestion_bus.models;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Entity
@Data
public class TypeAbonnement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
	private String stripePriceId;

    private String nom;

    private double prix;

    private int dureeMois;

    private boolean actif = true;

    private boolean isStudent;
}
