package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.dto.LigneDto;
import com.gestion.gestion_bus.dto.StationOrdreDto;
import com.gestion.gestion_bus.service.interfaces.LigneService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/lignes")
@RequiredArgsConstructor
public class LigneController {
    private final LigneService ligneService;

    @PreAuthorize("hasRole('ADMIN')")
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

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    ResponseEntity<Void> deleteLine(@PathVariable Long id){
        ligneService.deleteLine(id);
        return ResponseEntity.noContent().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}")
    ResponseEntity<LigneDto> updateLine(@PathVariable Long id,@RequestBody LigneDto updatedLine){
        return ResponseEntity.ok(ligneService.updateLine(id,updatedLine));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/station/new/{ligneId}")
    ResponseEntity<StationOrdreDto> addStation(@RequestBody StationOrdreDto stationOrdreDto,@PathVariable Long ligneId){
        return ResponseEntity.ok(ligneService.addStation(stationOrdreDto,ligneId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/ligne/{ligneId}/station/{stationId}")
    ResponseEntity<StationOrdreDto> updateStation(
            @RequestBody StationOrdreDto station,
            @PathVariable Long ligneId,
            @PathVariable Long stationId
    ){
        return ResponseEntity.ok(ligneService.updateStation(station,ligneId,stationId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/ligne/{ligneId}/station/{stationId}")
    ResponseEntity<Void> deleteStation(@PathVariable Long ligneId,@PathVariable Long stationId){
        ligneService.deleteStation(ligneId,stationId);
        return ResponseEntity.noContent().build();
    }

}
