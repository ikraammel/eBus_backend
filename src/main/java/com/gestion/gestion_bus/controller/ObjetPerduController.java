package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.dto.request.ObjetPerduRequestDto;
import com.gestion.gestion_bus.dto.response.ObjetPerduResponseDto;
import com.gestion.gestion_bus.enums.StatutObjet;
import com.gestion.gestion_bus.service.interfaces.ObjetPerduService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/objets")
@RequiredArgsConstructor
public class ObjetPerduController {

	private final ObjetPerduService objetPerduService;


	@GetMapping("/admin/all")
	public ResponseEntity<List<ObjetPerduResponseDto>> getAll() {
		return ResponseEntity.ok(objetPerduService.getAll());
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PutMapping("/{id}/statut")
	public ResponseEntity<ObjetPerduResponseDto> updateStatut(
		@PathVariable Long id,
		@RequestParam String statut
	) {
		StatutObjet statutEnum = StatutObjet.valueOf(statut.toUpperCase());
		return ResponseEntity.ok(objetPerduService.updateStatut(id, statutEnum));
	}

	@PreAuthorize("hasAnyRole('ADMIN','USER')")
	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		objetPerduService.delete(id);
		return ResponseEntity.noContent().build();
	}

	@PreAuthorize("hasRole('USER')")
	@PostMapping(value = "/declaration", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ResponseEntity<ObjetPerduResponseDto> declare(
			@ModelAttribute ObjetPerduRequestDto dto,
			@RequestPart(value = "image", required = false) MultipartFile image
	) {
		return ResponseEntity.ok(objetPerduService.declare(dto, image));
	}

	@GetMapping("/{id}")
	public ResponseEntity<ObjetPerduResponseDto> getById(@PathVariable Long id) {
		return ResponseEntity.ok(objetPerduService.getById(id));
	}

	@PreAuthorize("hasRole('ADMIN')")
	@PostMapping("/{id}/signaler-recupere")
	public ResponseEntity<ObjetPerduResponseDto> signalerRecupere(@PathVariable Long id) {
		return ResponseEntity.ok(objetPerduService.signalerRecupere(id));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<ObjetPerduResponseDto>> getByUser(@PathVariable Long userId) {
		return ResponseEntity.ok(objetPerduService.getByUserId(userId));
	}
}
