package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.Ligne;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LigneRepository extends JpaRepository<Ligne,Long> {
    Optional<Ligne> findByNumero(String numero);

}
