package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.Payment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<Payment,Long> {
}
