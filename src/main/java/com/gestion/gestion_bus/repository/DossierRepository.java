package com.gestion.gestion_bus.repository;


import com.gestion.gestion_bus.enums.StatusDossier;
import com.gestion.gestion_bus.models.Dossier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DossierRepository extends JpaRepository<Dossier, Long> {

    Optional<Dossier> findByUserId(Long userId);

    List<Dossier> findByStatus(StatusDossier status);
}