package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.dto.request.ObjetPerduRequestDto;
import com.gestion.gestion_bus.dto.response.ObjetPerduResponseDto;
import com.gestion.gestion_bus.enums.StatutObjet;
import com.gestion.gestion_bus.service.interfaces.ObjetPerduService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/objets")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ObjetPerduController {

	private final ObjetPerduService objetPerduService;

	// ─── ADMIN ─────────────────────────────────────────────

	@GetMapping("/admin/all")
	public ResponseEntity<List<ObjetPerduResponseDto>> getAllAdmin() {
		return ResponseEntity.ok(objetPerduService.getAll());
	}

	@PutMapping("/{id}/statut")
	public ResponseEntity<ObjetPerduResponseDto> updateStatut(
		@PathVariable Long id,
		@RequestParam String statut
	) {
		StatutObjet statutEnum = StatutObjet.valueOf(statut.toUpperCase());
		return ResponseEntity.ok(objetPerduService.updateStatut(id, statutEnum));
	}

	@DeleteMapping("/{id}")
	public ResponseEntity<Void> delete(@PathVariable Long id) {
		objetPerduService.delete(id);
		return ResponseEntity.noContent().build();
	}

	// ─── USER ─────────────────────────────────────────────

	@PostMapping("/declaration")
	public ResponseEntity<ObjetPerduResponseDto> declare(
		@Valid @RequestBody ObjetPerduRequestDto dto
	) {
		ObjetPerduResponseDto created = objetPerduService.declare(dto);
		return ResponseEntity.ok(created); // 🔥 FIX: OK au lieu de 201 (plus stable Flutter)
	}

	@GetMapping("/{id}")
	public ResponseEntity<ObjetPerduResponseDto> getById(@PathVariable Long id) {
		return ResponseEntity.ok(objetPerduService.getById(id));
	}

	@PostMapping("/{id}/signaler-recupere")
	public ResponseEntity<ObjetPerduResponseDto> signalerRecupere(@PathVariable Long id) {
		return ResponseEntity.ok(objetPerduService.signalerRecupere(id));
	}

	@GetMapping("/user/{userId}")
	public ResponseEntity<List<ObjetPerduResponseDto>> getByUser(@PathVariable Long userId) {
		return ResponseEntity.ok(objetPerduService.getByUserId(userId));
	}
}
