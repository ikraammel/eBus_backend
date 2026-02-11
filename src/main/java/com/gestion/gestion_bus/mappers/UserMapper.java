package com.gestion.gestion_bus.mappers;

import com.gestion.gestion_bus.dto.response.UserResponseDto;
import com.gestion.gestion_bus.models.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponseDto toDto(User user){
        UserResponseDto dto = new UserResponseDto();
        dto.setId(user.getId());
        dto.setNom(user.getNom());
        dto.setPrenom(user.getPrenom());
        dto.setEmail(user.getEmail());
        dto.setTel(user.getTel());
        dto.setAdresse(user.getAdresse());
        dto.setDateNaissance(user.getDateNaissance().toString());
        dto.setRole(user.getRole().getName());

        if (user.getAbonnement() != null && user.getAbonnement().getType() != null) {
            dto.setTypeAbonnement(user.getAbonnement().getType().name()); // .name() convertit enum -> String
        } else {
            dto.setTypeAbonnement(null);
        }

        return dto;
    }
}
