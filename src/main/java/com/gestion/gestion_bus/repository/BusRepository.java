package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.Bus;
import com.gestion.gestion_bus.enums.BusEtat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BusRepository extends JpaRepository<Bus, Long> {

    List<Bus> findByLigneId(Long ligneId);

    Optional<Bus> findByImmatriculation(String immatriculation);

    List<Bus> findByEtat(BusEtat etat);

    boolean existsByImmatriculation(String immatriculation);

    /**
     * Nombre de bus actifs
     */
    @Query("SELECT COUNT(b) FROM Bus b WHERE b.etat = 'ACTIVE'")
    long countActiveBuses();

    /**
     * Compte les bus par état (pour le graphique énergie)
     */
    @Query("SELECT b.etat, COUNT(b) FROM Bus b GROUP BY b.etat")
    List<Object[]> countByEtatGrouped();

    /**
     * Compte par état exact
     */
    long countByEtat(BusEtat etat);

    /**
     * Nombre de bus affectés à une ligne dont le nom est donné.
     * Utilisé pour enrichir les datasets d'occupation par ligne.
     */
    @Query("SELECT COUNT(b) FROM Bus b WHERE b.ligne.numero = :ligneNumero")
    long countByLigneNumero(@Param("ligneNumero") String ligneNumero);
}