package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.dto.BusDto;
import com.gestion.gestion_bus.service.interfaces.BusService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/bus")
@RequiredArgsConstructor
public class BusController {
    private final BusService busService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/new")
    ResponseEntity<BusDto> createBus(@RequestBody BusDto busDto){
        return ResponseEntity.ok(busService.createBus(busDto));
    }

    @GetMapping
    ResponseEntity<List<BusDto>> getAllBus(){
        return ResponseEntity.ok(busService.getAllBus());
    }

    @GetMapping("/{id}")
    ResponseEntity<BusDto> getBusById(@PathVariable Long id){
        return ResponseEntity.ok(busService.findById(id));
    }

    @GetMapping("/ligne/{ligneId}")
    ResponseEntity<List<BusDto>> getBusByLigne(@PathVariable Long ligneId){
        return ResponseEntity.ok(busService.getByLigne(ligneId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteBus(@PathVariable Long id){
        busService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    ResponseEntity<BusDto> updateBus(@PathVariable Long id,@RequestBody BusDto updatedBus){
        return ResponseEntity.ok(busService.updateBus(id,updatedBus));
    }

    @GetMapping("/immatriculation/{immatriculation}")
    ResponseEntity<BusDto> getBusByImmatriculation(@PathVariable String immatriculation){
        return ResponseEntity.ok(busService.findByImmatriculation(immatriculation));
    }

}
