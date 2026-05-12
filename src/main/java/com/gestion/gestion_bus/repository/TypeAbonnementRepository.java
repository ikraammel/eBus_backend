package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.TypeAbonnement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TypeAbonnementRepository extends JpaRepository<TypeAbonnement,Long> {

	List<TypeAbonnement> findByIsStudent(boolean isStudent);
}
