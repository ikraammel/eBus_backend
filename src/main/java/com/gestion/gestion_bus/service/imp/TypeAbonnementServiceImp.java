package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.models.TypeAbonnement;
import com.gestion.gestion_bus.repository.TypeAbonnementRepository;
import com.gestion.gestion_bus.service.interfaces.TypeAbonnementService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TypeAbonnementServiceImp implements TypeAbonnementService {

    private final TypeAbonnementRepository typeAbonnementRepository;

    @Override
    public List<TypeAbonnement> getAll() {
        return typeAbonnementRepository.findAll();
    }

    @Override
    public TypeAbonnement create(TypeAbonnement t) {
        return typeAbonnementRepository.save(t);
    }

    @Override
    public TypeAbonnement update(Long id, TypeAbonnement t) {
        TypeAbonnement existing = typeAbonnementRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Not found"));

        existing.setNom(t.getNom());
        existing.setPrix(t.getPrix());
        existing.setDureeMois(t.getDureeMois());
		existing.setStripePriceId(t.getStripePriceId());

        return typeAbonnementRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        typeAbonnementRepository.deleteById(id);
    }
}
