package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.dto.LigneDto;
import com.gestion.gestion_bus.service.interfaces.LigneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lignes")
@RequiredArgsConstructor
public class LigneController {
    private final LigneService ligneService;

    @PostMapping("/new")
    ResponseEntity<LigneDto> createLine(@RequestBody LigneDto ligneDto){
        return ResponseEntity.ok(ligneService.createLine(ligneDto));
    }

    @GetMapping
    ResponseEntity<List<LigneDto>> getAllLines(){
        return ResponseEntity.ok(ligneService.getAllLines());
    }

    @GetMapping("/{id}")
    ResponseEntity<LigneDto> getLineById(@PathVariable Long id){
        return ResponseEntity.ok(ligneService.getLineById(id));
    }

    @GetMapping("/ligne/{numero}")
    ResponseEntity<LigneDto> getLineById(@PathVariable String numero){
        return ResponseEntity.ok(ligneService.getLineByNumero(numero));
    }

    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteLine(@PathVariable Long id){
        ligneService.deleteLine(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}")
    ResponseEntity<LigneDto> updateLine(@PathVariable Long id,@RequestBody LigneDto updatedLine){
        return ResponseEntity.ok(ligneService.updateLine(id,updatedLine));
    }

}
