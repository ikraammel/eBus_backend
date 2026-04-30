package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.Trajet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TrajetRepository extends JpaRepository<Trajet, Long> {

    @Query("SELECT COUNT(t) FROM Trajet t WHERE t.onTime = true AND t.departureTime BETWEEN :start AND :end")
    long countOnTimeTrajets(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT SUM(t.distanceKm) FROM Trajet t WHERE t.departureTime BETWEEN :start AND :end")
    Double getTotalDistance(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT t.ligne, SUM(t.passengersCount) as totalRiders " +
            "FROM Trajet t " +
            "WHERE t.departureTime BETWEEN :start AND :end " +
            "GROUP BY t.ligne " +
            "ORDER BY totalRiders DESC")
    List<Object[]> findTopLines(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    // Commentez cette méthode temporairement
    // @Query("SELECT AVG(t.passengersCount * 100.0 / b.capacity) " +
    //        "FROM Trajet t JOIN t.bus b " +
    //        "WHERE t.departureTime BETWEEN :start AND :end")
    // Double getAverageOccupancyByPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}