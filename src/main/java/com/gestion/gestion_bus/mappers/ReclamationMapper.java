package com.gestion.gestion_bus.mappers;

import com.gestion.gestion_bus.dto.ReclamationDto;
import com.gestion.gestion_bus.models.Reclamation;
import org.springframework.stereotype.Component;

/**
 * @author $ {USER}
 **/
@Component
public class ReclamationMapper {
    public ReclamationDto toReclamationDto(Reclamation reclamation){
        ReclamationDto reclamationDto = new ReclamationDto();
        reclamationDto.setId(reclamation.getId());
        reclamationDto.setTitre(reclamation.getTitre());
        reclamationDto.setDescription(reclamation.getDescription());
        reclamationDto.setDate(reclamation.getDate());
        reclamationDto.setStatus(reclamation.getStatus());
        if(reclamation.getUser()!= null){
            reclamationDto.setUserId(reclamation.getUser().getId());
        }
        if (reclamation.getLigne() != null){
            reclamationDto.setLigneId(reclamation.getLigne().getId());
        }
        return reclamationDto;
    }

    public Reclamation toEntity(ReclamationDto reclamationDto){
        Reclamation reclamation = new Reclamation();
        reclamation.setId(reclamationDto.getId());
        reclamation.setTitre(reclamationDto.getTitre());
        reclamation.setDescription(reclamationDto.getDescription());
        reclamation.setDate(reclamationDto.getDate());
        reclamation.setStatus(reclamationDto.getStatus());
        return reclamation;
    }
}
