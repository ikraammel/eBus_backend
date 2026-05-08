package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.Abonnement;
import com.gestion.gestion_bus.enums.StatusAbonnement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Map;
import java.util.Optional;


public interface AbonnementRepository extends JpaRepository<Abonnement, Long> {

    Optional<Abonnement> findTopByUserIdOrderByIdDesc(Long userId);
    Optional<Abonnement> findTopByUserIdAndStatusOrderByIdDesc(Long userId, StatusAbonnement status);
    List<Abonnement> findAllByUserIdOrderByIdDesc(Long userId);
    Optional<Abonnement> findByUserId(Long userId);
    List<Abonnement> findTop5ByUserIdOrderByDateDebutDesc(Long userId);
    List<Abonnement> findTop20ByOrderByDateDebutDesc();


    /** Compte les abonnements par statut */
    long countByStatus(StatusAbonnement status);

    /** Somme totale des revenus des abonnements ACTIFS */
    @Query("SELECT COALESCE(SUM(a.prix), 0) FROM Abonnement a WHERE a.status = 'ACTIF'")
    double sumRevenuAbonnementsActifs();

    /**
     * Nombre d'abonnements par type.
     * Retourne des paires [typeNom, count]
     */
    @Query("SELECT a.typeAbonnement.nom, COUNT(a) FROM Abonnement a GROUP BY a.typeAbonnement.nom")
    List<Object[]> countParType();
}