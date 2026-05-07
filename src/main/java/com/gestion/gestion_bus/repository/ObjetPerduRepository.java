package com.gestion.gestion_bus.repository;

import com.gestion.gestion_bus.enums.StatutObjet;
import com.gestion.gestion_bus.enums.TypeAnnonce;
import com.gestion.gestion_bus.models.ObjetPerdu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObjetPerduRepository extends JpaRepository<ObjetPerdu, Long> {


	List<ObjetPerdu> findByUserId(Long userId);


	List<ObjetPerdu> findByStatut(StatutObjet statut);

	List<ObjetPerdu> findByType(TypeAnnonce type);

	List<ObjetPerdu> findByStatutAndType(StatutObjet statut, TypeAnnonce type);

    List<ObjetPerdu> findTop5ByUserIdOrderByDateDeclarationDesc(Long userId);

	List<ObjetPerdu> findTop20ByOrderByDateDeclarationDesc();
}
