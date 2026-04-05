package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.dto.ReclamationDto;
import com.gestion.gestion_bus.enums.ReclamationStatus;
import com.gestion.gestion_bus.service.interfaces.ReclamationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reclamations")
@RequiredArgsConstructor
public class ReclamationController {
    private final ReclamationService reclamationService;

    @GetMapping("/all")
    public ResponseEntity<List<ReclamationDto>> getAllReclamations(){
        return ResponseEntity.ok(reclamationService.getAllReclamations());
    }

    @GetMapping("/date/{date}")
    public ResponseEntity<List<ReclamationDto>> getReclamationsByDate(@PathVariable LocalDate date){
        return ResponseEntity.ok(reclamationService.getReclamationsByDate(date));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<ReclamationDto>> getReclamationsByStatus(@PathVariable ReclamationStatus status){
        return ResponseEntity.ok(reclamationService.getReclamationsByStatus(status));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ReclamationDto> getReclamationById(@PathVariable Long id){
        return ResponseEntity.ok(reclamationService.getReclamationById(id));
    }

    @PostMapping("/new")
    public ResponseEntity<ReclamationDto> createReclamation(@RequestBody ReclamationDto reclamationDto){
        return ResponseEntity.ok(reclamationService.createReclamation(reclamationDto));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReclamationDto> updateReclamation(@PathVariable Long id,@RequestBody ReclamationDto reclamationDto){
        return ResponseEntity.ok(reclamationService.updateReclamation(id,reclamationDto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReclamationDto> deleteReclamation(@PathVariable Long id){
        reclamationService.deleteReclamation(id);
        return ResponseEntity.noContent().build();
    }

}
