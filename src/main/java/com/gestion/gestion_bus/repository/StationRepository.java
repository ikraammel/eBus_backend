package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.Station;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StationRepository extends JpaRepository<Station,Long> {
}
