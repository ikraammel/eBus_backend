package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.LigneDto;
import com.gestion.gestion_bus.dto.StationOrdreDto;
import com.gestion.gestion_bus.mappers.LigneMapper;
import com.gestion.gestion_bus.models.Ligne;
import com.gestion.gestion_bus.models.LigneStation;
import com.gestion.gestion_bus.models.Station;
import com.gestion.gestion_bus.repository.LigneRepository;
import com.gestion.gestion_bus.repository.LigneStationRepository;
import com.gestion.gestion_bus.repository.StationRepository;
import com.gestion.gestion_bus.service.interfaces.LigneService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LigneServiceImp implements LigneService {

    private final LigneRepository ligneRepository;
    private final StationRepository stationRepository;
    private final LigneStationRepository ligneStationRepository;

    @Override
    public LigneDto createLine(LigneDto dto) {

        if(ligneRepository.findByNumero(dto.getNumero()).isPresent()){
            throw new RuntimeException("Ligne déjà existante");
        }
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
        return LigneMapper.toDto(ligneRepository.save(ligne));
    }

    @Override
    public StationOrdreDto addStation(StationOrdreDto stationDto, Long ligneId) {
        Ligne ligne = ligneRepository.findById(ligneId)
                .orElseThrow(() -> new RuntimeException("Ligne inexistante"));

        Station station = stationRepository.findByNom(stationDto.getNom())
                .orElseGet(() -> {
                    Station s = new Station();
                    s.setNom(stationDto.getNom());
                    s.setPosition(stationDto.getPosition());
                    return stationRepository.save(s);
                });

        LigneStation ls = new LigneStation();
        ls.setLigne(ligne);
        ls.setStation(station);
        ls.setOrdre(stationDto.getOrdre());
        ls.setDirection(stationDto.getDirection());
        ligne.getTrajets().add(ls);

        Ligne savedLigne = ligneRepository.save(ligne);

        LigneStation savedLs = savedLigne.getTrajets().stream()
                .filter(t -> t.getStation().getId().equals(station.getId()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Erreur lors de l'ajout de la station"));

        StationOrdreDto resultDto = new StationOrdreDto();
        resultDto.setId(savedLs.getStation().getId());
        resultDto.setNom(savedLs.getStation().getNom());
        resultDto.setOrdre(savedLs.getOrdre());
        resultDto.setDirection(savedLs.getDirection());
        resultDto.setPosition(savedLs.getStation().getPosition());
        return resultDto;
    }

    @Override
    public StationOrdreDto updateStation(StationOrdreDto stationDto, Long ligneId,Long stationId) {
        Ligne ligne = ligneRepository.findById(ligneId)
                .orElseThrow(() -> new RuntimeException("Ligne inexistante"));

        LigneStation ls = ligne.getTrajets().stream()
                .filter(t -> t.getStation().getId().equals(stationId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Station non trouvée sur cette ligne"));

        if(stationDto.getNom() != null && !stationDto.getNom().isEmpty()) {
            ls.getStation().setNom(stationDto.getNom());
        }
        if(stationDto.getPosition() != null) {
            ls.getStation().setPosition(stationDto.getPosition());
        }

        ls.setOrdre(stationDto.getOrdre());
        ls.setDirection(stationDto.getDirection());
        Ligne savedLigne = ligneRepository.save(ligne);

        LigneStation updatedLs = savedLigne.getTrajets().stream()
                .filter(t -> t.getStation().getId().equals(stationId))
                .findFirst()
                .get();

        StationOrdreDto updatedDto = new StationOrdreDto();
        updatedDto.setId(updatedLs.getStation().getId());
        updatedDto.setNom(updatedLs.getStation().getNom());
        updatedDto.setOrdre(updatedLs.getOrdre());
        updatedDto.setDirection(updatedLs.getDirection());
        updatedDto.setPosition(updatedLs.getStation().getPosition());

        return updatedDto;
    }

    @Override
    @Transactional
    public void deleteStation(Long ligneId, Long stationId) {
        Ligne ligne = ligneRepository.findById(ligneId)
                .orElseThrow(() -> new RuntimeException("Ligne inexistante"));

        LigneStation ls = ligne.getTrajets().stream()
                .filter(t -> t.getStation().getId().equals(stationId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Station non trouvée sur cette ligne"));

        ligne.getTrajets().remove(ls);
        ligneRepository.save(ligne);
        ligneStationRepository.deleteByStationId(stationId);
    }
}
