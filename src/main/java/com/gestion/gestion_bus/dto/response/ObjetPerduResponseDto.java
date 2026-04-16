package com.gestion.gestion_bus.dto.response;

import com.gestion.gestion_bus.enums.StatutObjet;
import com.gestion.gestion_bus.enums.TypeAnnonce;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ObjetPerduResponseDto {

	private Long id;
	private String nom;
	private String description;
	private String ligne;
	private String contact;

	private TypeAnnonce type;
	private StatutObjet statut;

	private LocalDateTime dateDeclaration;

	private Long userId;
	private String userNom;}
