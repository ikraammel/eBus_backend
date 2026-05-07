package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findAllByUserIdOrderByDatePaiementDesc(Long userId);

    Optional<Payment> findByAbonnementId(Long abonnementId);

    List<Payment> findTop5ByUserIdOrderByDatePaiementDesc(Long userId);

    List<Payment> findTop20ByOrderByDatePaiementDesc();
}
