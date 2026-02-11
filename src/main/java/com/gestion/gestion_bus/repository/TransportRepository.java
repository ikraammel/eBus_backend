package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.Transport;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransportRepository extends JpaRepository<Transport,Long> {
}
