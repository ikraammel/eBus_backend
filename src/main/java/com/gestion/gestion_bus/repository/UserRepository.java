package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.enums.StatusDossier;
import com.gestion.gestion_bus.models.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    boolean existsByCin(@NotBlank(message = "Le CIN est obligatoire") String cin);
    boolean existsByCne(String carteScolaire);
    boolean existsByTel(@NotBlank(message = "Le téléphone est obligatoire") String tel);
    Optional<User> findByTel(String tel);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.abonnements WHERE u.id = :id")
    Optional<User> findByIdWithAbonnements(Long id);

    /** Nombre d'users par statut de dossier */
    long countByStatusDossier(StatusDossier statusDossier);

    @Query("SELECT COUNT(u) FROM User u WHERE u.createdAt >= :since")
    long countUsersCreatedSince(@Param("since") LocalDateTime since);

    @Query("""
        SELECT YEAR(u.createdAt), MONTH(u.createdAt), COUNT(u)
        FROM User u
        WHERE u.createdAt >= :since
        GROUP BY YEAR(u.createdAt), MONTH(u.createdAt)
        ORDER BY YEAR(u.createdAt) ASC, MONTH(u.createdAt) ASC
    """)
    List<Object[]> countInscriptionsParMois(@Param("since") LocalDateTime since);
}