package com.gestion.gestion_bus.service.interfaces;

import com.gestion.gestion_bus.models.TypeAbonnement;

import java.util.List;

public interface TypeAbonnementService {
    List<TypeAbonnement> getAll();
    TypeAbonnement create(TypeAbonnement t);
    TypeAbonnement update(Long id, TypeAbonnement t);
    void delete(Long id);
}