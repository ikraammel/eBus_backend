package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.enums.ReclamationStatus;
import com.gestion.gestion_bus.models.Reclamation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ReclamationRepository extends JpaRepository<Reclamation, Long> {

    List<Reclamation> findByDate(LocalDate date);
    List<Reclamation> findByStatus(ReclamationStatus status);
    List<Reclamation> findTop5ByUserIdOrderByDateDesc(Long userId);
    List<Reclamation> findTop20ByOrderByDateDesc();

    long countByStatus(ReclamationStatus status);
}
