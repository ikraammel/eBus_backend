package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.Bus;
import com.gestion.gestion_bus.enums.BusEtat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {

    List<Bus> findByLigneId(Long ligneId);
    Optional<Bus> findByImmatriculation(String immatriculation);
    List<Bus> findByEtat(BusEtat etat);

    @Query("SELECT COUNT(b) FROM Bus b WHERE b.etat = 'ACTIF'")
    long countActiveBuses();
}