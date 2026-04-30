package com.gestion.gestion_bus.controller;


import com.gestion.gestion_bus.dto.response.DossierResponseDto;
import com.gestion.gestion_bus.service.interfaces.DossierService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dossiers")
@RequiredArgsConstructor
public class AdminDossierController {

    private final DossierService adminService;

    @GetMapping
    public List<DossierResponseDto> getAllDossiers() {
        return adminService.getAllDossiers();
    }

    @GetMapping("/en-attente")
    public List<DossierResponseDto> getPending() {
        return adminService.getDossiersEnAttente();
    }

    @GetMapping("/{id}")
    public DossierResponseDto getOne(@PathVariable Long id) {
        return adminService.getDossier(id);
    }

    @PatchMapping("/{id}/valider")
    public void valider(@PathVariable Long id) {
        adminService.validerDossier(id);
    }

    @PatchMapping("/{id}/rejeter")
    public void rejeter(@PathVariable Long id) {
        adminService.rejeterDossier(id);
    }

}
