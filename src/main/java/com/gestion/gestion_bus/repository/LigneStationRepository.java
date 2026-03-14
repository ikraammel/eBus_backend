package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.LigneStation;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * @author $ {USER}
 **/
public interface LigneStationRepository extends JpaRepository<LigneStation,Long> {
    void deleteByStationId(Long stationId);
}
