package com.gestion.gestion_bus.controller;


import com.gestion.gestion_bus.dto.request.RejectRequest;
import com.gestion.gestion_bus.dto.response.DossierResponseDto;
import com.gestion.gestion_bus.service.interfaces.DossierService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/dossiers")
@RequiredArgsConstructor
public class AdminDossierController {

    private final DossierService adminService;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<DossierResponseDto> getAllDossiers() {
        return adminService.getAllDossiers();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/en-attente")
    public List<DossierResponseDto> getPending() {
        return adminService.getDossiersEnAttente();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public DossierResponseDto getOne(@PathVariable Long id) {
        return adminService.getDossier(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/valider")
    public void valider(@PathVariable Long id) {
        adminService.validerDossier(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/rejeter")
    public void rejeter(@PathVariable Long id,@RequestBody RejectRequest request) {
        adminService.rejeterDossier(id,request.getRejectionReason());
    }

}
