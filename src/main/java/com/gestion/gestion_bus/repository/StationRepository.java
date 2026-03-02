package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface StationRepository extends JpaRepository<Station,Long> {
    Optional<Station> findByNom(String nom);
}
