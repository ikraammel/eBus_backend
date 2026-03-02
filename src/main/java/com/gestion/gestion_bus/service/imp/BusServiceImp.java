package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.BusDto;
import com.gestion.gestion_bus.mappers.BusMapper;
import com.gestion.gestion_bus.models.Bus;
import com.gestion.gestion_bus.models.Ligne;
import com.gestion.gestion_bus.repository.BusRepository;
import com.gestion.gestion_bus.repository.LigneRepository;
import com.gestion.gestion_bus.service.interfaces.BusService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BusServiceImp implements BusService {
    private final BusRepository busRepository;
    private final LigneRepository ligneRepository;

    @Override
    public BusDto createBus(BusDto dto) {
        Bus bus = new Bus();

        Ligne ligne = ligneRepository.findById(dto.getLigneId())
                .orElseThrow(() -> new RuntimeException("Ligne not found"));
        bus.setEtat(dto.getEtat());
        bus.setLigne(ligne);
        Bus savedBus = busRepository.save(bus);
        return BusMapper.toDto(savedBus);
    }

    @Override
    public List<BusDto> getAllBus() {
        return busRepository.findAll()
                .stream()
                .map(BusMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BusDto findById(Long id) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bus not found"));
        return BusMapper.toDto(bus);
    }

    @Override
    public List<BusDto> getByLigne(Long ligneId) {
        return busRepository.findByLigneId(ligneId)
                .stream()
                .map(BusMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(Long id) {
        busRepository.deleteById(id);
    }

    @Override
    public BusDto updateBus(Long id, BusDto dto) {
        Bus bus = busRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bus not found"));
        Ligne ligne = ligneRepository.findById(dto.getLigneId())
                .orElseThrow(() -> new RuntimeException("Ligne not found"));
        bus.setEtat(dto.getEtat());
        bus.setLigne(ligne);
        bus.setImmatriculation(dto.getImmatriculation());
        Bus savedBus = busRepository.save(bus);
        return BusMapper.toDto(savedBus);
    }

    @Override
    public BusDto findByImmatriculation(String immatriculation) {
        Bus bus = busRepository.findByImmatriculation(immatriculation)
                .orElseThrow(() -> new RuntimeException("Bus not found"));
        return BusMapper.toDto(bus);
    }
}
