package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.User;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User,Long> {
    boolean existsByEmail(String email);

    Optional<User> findByEmail(String email);

    boolean existsByCin(@NotBlank(message = "Le CIN est obligatoire") String cin);

    boolean existsByCne(String carteScolaire);

    boolean existsByTel(@NotBlank(message = "Le téléphone est obligatoire") String tel);

    Optional<User> findByTel(String tel);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.abonnements WHERE u.id = :id")
    Optional<User> findByIdWithAbonnements(Long id);

}
