package com.gestion.gestion_bus.dto;

import com.gestion.gestion_bus.enums.ReclamationStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * @author $ {USER}
 **/
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReclamationDto {
    private Long id;

    private String titre;
    private String description;
    private ReclamationStatus status;
    private LocalDate date;
    private Long userId;
    private Long ligneId;
}
