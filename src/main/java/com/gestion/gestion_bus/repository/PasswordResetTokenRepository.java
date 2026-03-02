package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.ResetPasswordToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<ResetPasswordToken,Long> {
    void deleteByEmail(String email);

    Optional<ResetPasswordToken> findByToken(String token);
}
