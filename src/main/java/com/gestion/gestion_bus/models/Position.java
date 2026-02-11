package com.gestion.gestion_bus.models;

import jakarta.persistence.Embeddable;

@Embeddable
public class Position {
    private double latitude;
    private double longitude;
}
