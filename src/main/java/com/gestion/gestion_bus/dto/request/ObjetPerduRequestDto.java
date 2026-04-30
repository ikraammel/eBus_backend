package com.gestion.gestion_bus.dto.request;

import com.gestion.gestion_bus.enums.StatutObjet;
import com.gestion.gestion_bus.enums.TypeAnnonce;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class ObjetPerduRequestDto {

	private String nom;
	private String description;
	private Long ligneId;
	private String contact;
	private TypeAnnonce type;
	private StatutObjet statut;
	private String imageUrl;

	private Long userId;
	private LocalDateTime dateDeclaration;
}
