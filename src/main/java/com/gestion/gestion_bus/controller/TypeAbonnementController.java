package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.models.TypeAbonnement;
import com.gestion.gestion_bus.service.interfaces.TypeAbonnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/types-abonnement")
@RequiredArgsConstructor
public class TypeAbonnementController {

    private final TypeAbonnementService service;

    @GetMapping
    public List<TypeAbonnement> getAll() {
        return service.getAll();
    }

    @PostMapping
    public TypeAbonnement create(@RequestBody TypeAbonnement t) {
        return service.create(t);
    }

    @PutMapping("/{id}")
    public TypeAbonnement update(@PathVariable Long id, @RequestBody TypeAbonnement t) {
        return service.update(id, t);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}