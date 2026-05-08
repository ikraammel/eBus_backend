package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    /** Somme du revenu de tous les tickets vendus */
    @Query("SELECT COALESCE(SUM(t.prix), 0) FROM Ticket t")
    double sumRevenuTickets();
}