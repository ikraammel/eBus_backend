package com.gestion.gestion_bus.service.interfaces;

import com.gestion.gestion_bus.dto.ReclamationDto;
import com.gestion.gestion_bus.enums.ReclamationStatus;

import java.time.LocalDate;
import java.util.List;


public interface ReclamationService {
    List<ReclamationDto> getAllReclamations();
    List<ReclamationDto> getReclamationsByDate(LocalDate date);
    List<ReclamationDto> getReclamationsByStatus(ReclamationStatus status);
    ReclamationDto getReclamationById(Long id);
    ReclamationDto createReclamation(ReclamationDto reclamationDto);
    ReclamationDto updateReclamation(Long id,ReclamationDto reclamationDto);
    void deleteReclamation(Long id);
}
