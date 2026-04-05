package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.ReclamationDto;
import com.gestion.gestion_bus.enums.ReclamationStatus;
import com.gestion.gestion_bus.mappers.ReclamationMapper;
import com.gestion.gestion_bus.models.Ligne;
import com.gestion.gestion_bus.models.Reclamation;
import com.gestion.gestion_bus.models.User;
import com.gestion.gestion_bus.repository.LigneRepository;
import com.gestion.gestion_bus.repository.ReclamationRepository;
import com.gestion.gestion_bus.repository.UserRepository;
import com.gestion.gestion_bus.service.interfaces.ReclamationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReclamationServiceImp implements ReclamationService {
    private final ReclamationRepository reclamationRepository;
    private final ReclamationMapper reclamationMapper;
    private final UserRepository userRepository;
    private final LigneRepository ligneRepository;

    @Override
    public List<ReclamationDto> getAllReclamations() {
        return reclamationRepository.findAll()
                .stream()
                .map(reclamationMapper::toReclamationDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReclamationDto> getReclamationsByDate(LocalDate date) {
        return reclamationRepository.findByDate(date)
                .stream()
                .map(reclamationMapper::toReclamationDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReclamationDto> getReclamationsByStatus(ReclamationStatus status) {
        return reclamationRepository.findByStatus(status)
                .stream()
                .map(reclamationMapper::toReclamationDto)
                .collect(Collectors.toList());
    }

    @Override
    public ReclamationDto getReclamationById(Long id) {
        Reclamation reclamation = reclamationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Reclamation non trouvée"));
        return reclamationMapper.toReclamationDto(reclamation);
    }

    @Override
    public ReclamationDto createReclamation(ReclamationDto reclamationDto) {
        User user = userRepository.findById(reclamationDto.getUserId())
                .orElseThrow(() -> new RuntimeException("User non trouvé"));

        Ligne ligne = ligneRepository.findById(reclamationDto.getLigneId())
                .orElseThrow(() -> new RuntimeException("Ligne non trouvée"));

        Reclamation reclamation = reclamationMapper.toEntity(reclamationDto);
        reclamation.setUser(user);
        reclamation.setLigne(ligne);
        reclamation.setStatus(ReclamationStatus.EN_ATTENTE);
        Reclamation savedReclamation = reclamationRepository.save(reclamation);

        return reclamationMapper.toReclamationDto(savedReclamation);
    }

    @Override
    public ReclamationDto updateReclamation(Long id, ReclamationDto reclamationDto) {
        Reclamation reclamationToUpdate = reclamationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Réclamation introuvable"));

        if (reclamationDto.getTitre() != null) {
            reclamationToUpdate.setTitre(reclamationDto.getTitre());
        }
        if (reclamationDto.getStatus() != null) {
            reclamationToUpdate.setStatus(reclamationDto.getStatus());
        }
        if (reclamationDto.getDescription() != null) {
            reclamationToUpdate.setDescription(reclamationDto.getDescription());
        }
        if (reclamationDto.getDate() != null) {
            reclamationToUpdate.setDate(reclamationDto.getDate());
        }
        if (reclamationDto.getUserId() != null) {
            User user = userRepository.findById(reclamationDto.getUserId())
                    .orElseThrow(() -> new RuntimeException("User non trouvé"));
            reclamationToUpdate.setUser(user);
        }
        if (reclamationDto.getLigneId() != null) {
            Ligne ligne = ligneRepository.findById(reclamationDto.getLigneId())
                    .orElseThrow(() -> new RuntimeException("Ligne non trouvée"));
            reclamationToUpdate.setLigne(ligne);
        }

        Reclamation updatedReclamation = reclamationRepository.save(reclamationToUpdate);

        return reclamationMapper.toReclamationDto(updatedReclamation);
    }

    @Override
    public void deleteReclamation(Long id) {
        if (!reclamationRepository.existsById(id)) {
            throw new RuntimeException("Réclamation introuvable");
        }
        reclamationRepository.deleteById(id);
    }
}
