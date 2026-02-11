package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.Bus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BusRepository extends JpaRepository<Bus,Long> {
}
