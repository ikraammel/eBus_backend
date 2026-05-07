package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.Abonnement;
import com.gestion.gestion_bus.enums.StatusAbonnement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {


    Optional<Abonnement> findTopByUserIdOrderByIdDesc(Long userId);


    Optional<Abonnement> findTopByUserIdAndStatusOrderByIdDesc(Long userId, StatusAbonnement status);


    List<Abonnement> findAllByUserIdOrderByIdDesc(Long userId);


    Optional<Abonnement> findByUserId(Long userId);
}
