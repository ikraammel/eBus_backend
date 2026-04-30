package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.LigneStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LigneStationRepository extends JpaRepository<LigneStation,Long> {
    void deleteByLigneIdAndStationId(Long ligneId, Long stationId);
}
