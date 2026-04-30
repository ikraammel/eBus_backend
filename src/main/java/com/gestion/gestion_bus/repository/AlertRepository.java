package com.gestion.gestion_bus.repository;
import com.gestion.gestion_bus.models.Alert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByStatus(Alert.AlertStatus status);
    List<Alert> findBySeverity(Alert.Severity severity);
}