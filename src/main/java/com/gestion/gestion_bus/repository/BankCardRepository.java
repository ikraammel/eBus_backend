package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.models.BankCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BankCardRepository extends JpaRepository<BankCard, Long> {

    List<BankCard> findByUserId(Long userId);

    Optional<BankCard> findByUserIdAndIsMainTrue(Long userId);

    boolean existsByUserIdAndIsMainTrue(Long userId);
}
