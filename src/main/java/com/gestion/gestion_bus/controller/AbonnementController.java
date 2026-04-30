package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.dto.AbonnementDto;
import com.gestion.gestion_bus.enums.StatusAbonnement;
import com.gestion.gestion_bus.service.interfaces.AbonnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/abonnements")
@RequiredArgsConstructor
public class AbonnementController {

    private final AbonnementService service;

    @PostMapping
    public AbonnementDto create(
            @RequestParam Long userId,
            @RequestParam Long typeId
    ) {
        return service.createAbonnement(userId, typeId);
    }

    @GetMapping
    public List<AbonnementDto> getAll() {
        return service.getAllAbonnements();
    }

    @GetMapping("/user/{userId}")
    public AbonnementDto getByUser(@PathVariable Long userId) {
        return service.getAbonnementByUser(userId);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.deleteAbonnement(id);
    }

    @PatchMapping("/{id}/status")
    public AbonnementDto updateStatus(
            @PathVariable Long id,
            @RequestParam StatusAbonnement status
    ) {
        return service.updateStatus(id, status);
    }
}