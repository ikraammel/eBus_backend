package com.gestion.gestion_bus.service.interfaces;

import com.gestion.gestion_bus.dto.request.ObjetPerduRequestDto;
import com.gestion.gestion_bus.dto.response.ObjetPerduResponseDto;
import com.gestion.gestion_bus.enums.StatutObjet;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ObjetPerduService {

	List<ObjetPerduResponseDto> getAll();

	ObjetPerduResponseDto getById(Long id);

	ObjetPerduResponseDto declare(ObjetPerduRequestDto dto, MultipartFile image);

	ObjetPerduResponseDto updateStatut(Long id, StatutObjet statut);

	ObjetPerduResponseDto signalerRecupere(Long id);

	void delete(Long id);

	List<ObjetPerduResponseDto> getByUserId(Long userId);
}
