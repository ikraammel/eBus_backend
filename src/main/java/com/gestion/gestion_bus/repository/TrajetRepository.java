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

    /** Trajets à l'heure dans une plage horaire */
    @Query("SELECT COUNT(t) FROM Trajet t WHERE t.onTime = true AND t.departureTime BETWEEN :start AND :end")
    long countOnTimeTrajets(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** Nombre total de trajets dans une plage horaire */
    @Query("SELECT COUNT(t) FROM Trajet t WHERE t.departureTime BETWEEN :start AND :end")
    long countTrajetsBetween(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /** Distance totale parcourue dans une plage horaire */
    @Query("SELECT SUM(t.distanceKm) FROM Trajet t WHERE t.departureTime BETWEEN :start AND :end")
    Double getTotalDistance(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Taux d'occupation moyen (passagers / capacité du bus) en pourcentage.
     * Nécessite que Trajet soit lié à Bus (qui a une capacité).
     */
    @Query("SELECT AVG(t.passengersCount * 100.0 / b.capacity) " +
            "FROM Trajet t JOIN t.bus b " +
            "WHERE t.departureTime BETWEEN :start AND :end " +
            "AND b.capacity > 0")
    Double getAverageOccupancyRate(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    /**
     * Taux d'occupation moyen pour une ligne donnée dans une plage horaire.
     */
    @Query("SELECT AVG(t.passengersCount * 100.0 / b.capacity) " +
            "FROM Trajet t JOIN t.bus b " +
            "WHERE t.ligne.numero = :ligne " +
            "AND t.departureTime BETWEEN :start AND :end " +
            "AND b.capacity > 0")
    Double getOccupancyRateByLigneAndPeriod(
            @Param("ligne") String ligne,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    /**
     * Lignes les plus fréquentées (par nombre de passagers) dans une plage horaire.
     * Retourne des paires [ligneName, totalPassengers].
     */
    @Query("SELECT t.ligne.numero, SUM(t.passengersCount) AS totalRiders " +
            "FROM Trajet t " +
            "WHERE t.departureTime BETWEEN :start AND :end " +
            "GROUP BY t.ligne.numero " +
            "ORDER BY totalRiders DESC")
    List<Object[]> findTopLines(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}