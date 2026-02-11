package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.Ligne;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LigneRepository extends JpaRepository<Ligne,Long> {
}
