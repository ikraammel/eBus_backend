package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.dto.BusDto;
import com.gestion.gestion_bus.models.Bus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public interface BusRepository extends JpaRepository<Bus,Long> {
    List<Bus> findByLigneId(Long ligneId);

    Optional<Bus> findByImmatriculation(String immatriculation);
}
