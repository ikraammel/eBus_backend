package com.gestion.gestion_bus.mappers;

import com.gestion.gestion_bus.dto.response.ObjetPerduResponseDto;
import com.gestion.gestion_bus.models.ObjetPerdu;
import org.springframework.stereotype.Component;

@Component
public class ObjetPerduMapper {

	public ObjetPerduResponseDto toDto(ObjetPerdu objet) {
		if (objet == null) return null;

		return ObjetPerduResponseDto.builder()
			.id(objet.getId())
			.nom(objet.getNom())
			.description(objet.getDescription())
			.ligneId(objet.getLigne() != null ? objet.getLigne().getId() : null)
			.ligneNom(objet.getLigne() != null ? objet.getLigne().getNumero() : null)
			.contact(objet.getContact())
			.type(objet.getType())
			.statut(objet.getStatut())
			.imageUrl(objet.getImageUrl())
			.dateDeclaration(objet.getDateDeclaration())
			.userId(objet.getUser() != null ? objet.getUser().getId() : null)
			.userNom(objet.getUser() != null ? objet.getUser().getNom() : null)
			.build();
	}
}
