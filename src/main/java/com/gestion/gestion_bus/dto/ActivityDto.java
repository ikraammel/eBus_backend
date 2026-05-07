package com.gestion.gestion_bus.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ActivityDto {
    private String type;
    private String description;
    private LocalDateTime date;
    private Long userId;
    private String userNom;
    private String userPrenom;
}
