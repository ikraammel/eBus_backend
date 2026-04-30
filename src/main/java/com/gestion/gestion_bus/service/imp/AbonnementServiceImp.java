package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.AbonnementDto;
import com.gestion.gestion_bus.enums.StatusAbonnement;
import com.gestion.gestion_bus.mappers.AbonnementMapper;
import com.gestion.gestion_bus.models.Abonnement;
import com.gestion.gestion_bus.models.TypeAbonnement;
import com.gestion.gestion_bus.models.User;
import com.gestion.gestion_bus.repository.AbonnementRepository;
import com.gestion.gestion_bus.repository.TypeAbonnementRepository;
import com.gestion.gestion_bus.repository.UserRepository;
import com.gestion.gestion_bus.service.interfaces.AbonnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AbonnementServiceImp implements AbonnementService {

    private final AbonnementRepository abonnementRepository;
    private final UserRepository userRepository;
    private final TypeAbonnementRepository typeAbonnementRepository;

    public AbonnementDto mapWithEffectiveStatus(Abonnement a) {

        AbonnementDto dto = AbonnementMapper.mapToDto(a);

        if (a.getDateFin() != null && a.getDateFin().isBefore(LocalDate.now())) {
            dto.setStatus(StatusAbonnement.EXPIRE);
        }

        return dto;
    }

    @Override
    public AbonnementDto createAbonnement(Long userId, Long typeId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        TypeAbonnement type = typeAbonnementRepository.findById(typeId)
                .orElseThrow(() -> new RuntimeException("Type abonnement not found"));

        if (!type.isActif())
            throw new RuntimeException("Type abonnement désactivé");

        Abonnement a = new Abonnement();
        a.setUser(user);
        a.setTypeAbonnement(type);
        a.setPrix(type.getPrix());

        a.setDateDebut(LocalDate.now());
        a.setDateFin(LocalDate.now().plusMonths(type.getDureeMois()));

        a.setStatus(StatusAbonnement.EN_ATTENTE);

        Abonnement saved = abonnementRepository.save(a);

         return mapWithEffectiveStatus(saved);
    }

    @Override
    public AbonnementDto updateStatus(Long id, StatusAbonnement status) {

        Abonnement a = abonnementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Abonnement not found"));

        a.setStatus(status);

        abonnementRepository.save(a);

        return mapWithEffectiveStatus(a);
    }

    @Override
    public AbonnementDto getAbonnementByUser(Long userId) {

        Abonnement a = abonnementRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Abonnement not found"));

        return mapWithEffectiveStatus(a);
    }

    @Override
    public List<AbonnementDto> getAllAbonnements() {
        return abonnementRepository.findAll()
                .stream()
                .map(this::mapWithEffectiveStatus)
                .toList();
    }

    @Override
    public void deleteAbonnement(Long id) {
        abonnementRepository.deleteById(id);
    }
}