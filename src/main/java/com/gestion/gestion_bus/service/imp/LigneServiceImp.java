package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.LigneDto;
import com.gestion.gestion_bus.mappers.LigneMapper;
import com.gestion.gestion_bus.models.Ligne;
import com.gestion.gestion_bus.models.LigneStation;
import com.gestion.gestion_bus.models.Station;
import com.gestion.gestion_bus.repository.LigneRepository;
import com.gestion.gestion_bus.repository.StationRepository;
import com.gestion.gestion_bus.service.interfaces.LigneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LigneServiceImp implements LigneService {

    private final LigneRepository ligneRepository;
    private final StationRepository stationRepository;

    @Override
    public LigneDto createLine(LigneDto dto) {

        Ligne ligne = new Ligne();
        ligne.setDistance(dto.getDistance());
        ligne.setNumero(dto.getNumero());
        ligne.setStartPoint(dto.getStartPoint());
        ligne.setEndPoint(dto.getEndPoint());

        List<LigneStation> trajets = dto.getStations().stream().map(s -> {

            LigneStation ls = new LigneStation();
            ls.setLigne(ligne);

            // Vérifier si station existe
            Station station = stationRepository.findByNom(s.getNom())
                    .orElseGet(() -> {
                        Station newStation = new Station();
                        newStation.setNom(s.getNom());
                        return stationRepository.save(newStation);
                    });

            ls.setStation(station);
            ls.setOrdre(s.getOrdre());
            ls.setDirection(s.getDirection());

            return ls;

        }).toList();

        ligne.setTrajets(trajets);

        return LigneMapper.toDto(ligneRepository.save(ligne));
    }

    @Override
    public List<LigneDto> getAllLines() {
        return ligneRepository.findAll()
                .stream()
                .map(LigneMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public LigneDto getLineById(Long id) {
        Ligne ligne = ligneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ligne introuvable"));
        return LigneMapper.toDto(ligne);
    }

    @Override
    public LigneDto getLineByNumero(String numero) {
        Ligne ligne = ligneRepository.findByNumero(numero)
                .orElseThrow(() -> new RuntimeException("Ligne introuvable"));
        return LigneMapper.toDto(ligne);
    }

    @Override
    public void deleteLine(Long id) {
        ligneRepository.deleteById(id);
    }

    @Override
    public LigneDto updateLine(Long id, LigneDto dto) {

        Ligne ligne = ligneRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ligne introuvable"));

        ligne.setNumero(dto.getNumero());
        ligne.setStartPoint(dto.getStartPoint());
        ligne.setEndPoint(dto.getEndPoint());
        ligne.setDistance(dto.getDistance());

        // Supprimer anciens trajets
        ligne.getTrajets().clear();

        if (dto.getStations() != null) {
            List<LigneStation> newTrajets = dto.getStations().stream().map(s -> {

                LigneStation ls = new LigneStation();
                ls.setLigne(ligne);

                Station station = stationRepository.findByNom(s.getNom())
                        .orElseGet(() -> {
                            Station newStation = new Station();
                            newStation.setNom(s.getNom());
                            return stationRepository.save(newStation);
                        });

                ls.setStation(station);
                ls.setOrdre(s.getOrdre());
                ls.setDirection(s.getDirection());

                return ls;

            }).toList();

            ligne.getTrajets().addAll(newTrajets);
        }

        return LigneMapper.toDto(ligneRepository.save(ligne));
    }
}
