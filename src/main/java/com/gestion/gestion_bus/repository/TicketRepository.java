package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketRepository extends JpaRepository<Ticket,Long> {
}
