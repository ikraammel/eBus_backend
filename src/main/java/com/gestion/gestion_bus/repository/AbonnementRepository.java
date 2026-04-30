package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.Abonnement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AbonnementRepository extends JpaRepository<Abonnement,Long> {
    Optional<Abonnement> findByUserId(Long userId);
}
