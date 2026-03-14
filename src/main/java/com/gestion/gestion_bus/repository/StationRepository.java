package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.Station;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StationRepository extends JpaRepository<Station,Long> {
    List<Station> findByNom(String nom);
    Optional<Station> findFirstByNom(String nom);
}
