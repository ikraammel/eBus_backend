package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.response.ObjetPerduResponseDto;
import com.gestion.gestion_bus.enums.StatutObjet;
import com.gestion.gestion_bus.models.Bus;
import com.gestion.gestion_bus.models.Ligne;
import com.gestion.gestion_bus.models.ObjetPerdu;
import com.gestion.gestion_bus.models.User;
import com.gestion.gestion_bus.repository.BusRepository;
import com.gestion.gestion_bus.repository.LigneRepository;
import com.gestion.gestion_bus.repository.ObjetPerduRepository;
import com.gestion.gestion_bus.repository.UserRepository;
import com.gestion.gestion_bus.service.interfaces.ObjetPerduService;
import com.gestion.gestion_bus.service.imp.FileStorageService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.gestion.gestion_bus.mappers.ObjetPerduMapper;
import com.gestion.gestion_bus.dto.request.ObjetPerduRequestDto;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ObjetPerduServiceImp implements ObjetPerduService {

	private final ObjetPerduRepository objetPerduRepository;
	private final UserRepository userRepository;
	private final LigneRepository ligneRepository;
	private final BusRepository busRepository;
	private final ObjetPerduMapper objetPerduMapper;
	private final FileStorageService fileStorageService;

	@Override
	@Transactional(readOnly = true)
	public List<ObjetPerduResponseDto> getAll() {
		return objetPerduRepository.findAll()
			.stream()
			.map(objetPerduMapper::toDto)
			.collect(Collectors.toList());
	}

	@Override
	@Transactional(readOnly = true)
	public ObjetPerduResponseDto getById(Long id) {
		ObjetPerdu objet = objetPerduRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("Objet #" + id + " introuvable"));
		return objetPerduMapper.toDto(objet);
	}

	@Override
	public ObjetPerduResponseDto declare(ObjetPerduRequestDto dto, MultipartFile image) {
		ObjetPerdu objet = new ObjetPerdu();
		objet.setNom(dto.getNom());
		objet.setDescription(dto.getDescription());
		Ligne ligne = ligneRepository.findById(dto.getLigneId())
				.orElseThrow(() -> new RuntimeException("Ligne introuvable"));

		objet.setLigne(ligne);
		objet.setContact(dto.getContact());
		objet.setType(dto.getType());
		objet.setStatut(dto.getStatut() != null ? dto.getStatut() : StatutObjet.EN_ATTENTE);

		objet.setDateDeclaration(
			dto.getDateDeclaration() != null ? dto.getDateDeclaration() : LocalDateTime.now()
		);

		if (dto.getUserId() != null) {
			User user = userRepository.findById(dto.getUserId()).orElse(null);
			objet.setUser(user);
		}

		if (dto.getBusId() != null) {
			Bus bus = busRepository.findById(dto.getBusId())
				.orElseThrow(() -> new RuntimeException("Bus introuvable"));
			objet.setBus(bus);
		}

		if (image != null && !image.isEmpty()) {
			String imageUrl = fileStorageService.save(image);
			objet.setImageUrl(imageUrl);
		}

		ObjetPerdu saved = objetPerduRepository.save(objet);
		return objetPerduMapper.toDto(saved);
	}

	@Override
	public ObjetPerduResponseDto updateStatut(Long id, StatutObjet statut) {
		ObjetPerdu objet = objetPerduRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("Objet #" + id + " introuvable"));
		objet.setStatut(statut);
		return objetPerduMapper.toDto(objetPerduRepository.save(objet));
	}

	@Override
	public ObjetPerduResponseDto signalerRecupere(Long id) {
		ObjetPerdu objet = objetPerduRepository.findById(id)
			.orElseThrow(() -> new EntityNotFoundException("Objet #" + id + " introuvable"));

		if (objet.getStatut() != StatutObjet.DISPONIBLE) {
			throw new IllegalStateException("L'objet doit être DISPONIBLE pour être signalé récupéré");
		}

		objet.setStatut(StatutObjet.EN_ATTENTE_RECUPERATION);
		return objetPerduMapper.toDto(objetPerduRepository.save(objet));
	}

	@Override
	public void delete(Long id) {
		if (!objetPerduRepository.existsById(id)) {
			throw new EntityNotFoundException("Objet #" + id + " introuvable");
		}
		objetPerduRepository.deleteById(id);
	}

	@Override
	@Transactional(readOnly = true)
	public List<ObjetPerduResponseDto> getByUserId(Long userId) {
		return objetPerduRepository.findByUserId(userId)
			.stream()
			.map(objetPerduMapper::toDto)
			.collect(Collectors.toList());
	}
}
