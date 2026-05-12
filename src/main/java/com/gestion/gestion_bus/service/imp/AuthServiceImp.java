package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.request.LoginRequestDto;
import com.gestion.gestion_bus.dto.request.UserRequestDto;
import com.gestion.gestion_bus.dto.response.AuthenticationResponse;
import com.gestion.gestion_bus.dto.response.UserResponseDto;
import com.gestion.gestion_bus.enums.StatusAbonnement;
import com.gestion.gestion_bus.enums.StatusDossier;
import com.gestion.gestion_bus.mappers.UserMapper;
import com.gestion.gestion_bus.models.*;
import com.gestion.gestion_bus.repository.*;
import com.gestion.gestion_bus.service.interfaces.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthServiceImp implements AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AbonnementRepository abonnementRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final TypeAbonnementRepository typeAbonnementRepository;
    private final DossierRepository dossierRepository;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    public AuthenticationResponse login(LoginRequestDto login) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        login.getEmail(),
                        login.getPassword()
                )
        );

        User user = userRepository.findByEmail(login.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String jwt = jwtService.generateToken(user);

        return new AuthenticationResponse(jwt,userMapper.toDto(user));
    }

    @Override
    @Transactional
    public UserResponseDto registerUser(UserRequestDto dto,
                                        MultipartFile photo,
                                        MultipartFile carteScolaire,
                                        MultipartFile cinPhoto) {

        verifyChamps(dto, photo, carteScolaire, cinPhoto);

        String cinClean = dto.getCin().trim();
        String cne = dto.getCne() != null ? dto.getCne().trim() : null;

        verifyUnicity(dto, cinClean, cne);

        TypeAbonnement type = typeAbonnementRepository.findById(dto.getAbonnementId())
                .orElseThrow(() -> new RuntimeException("Type abonnement introuvable"));

        if (!type.isActif()) {
            throw new RuntimeException("Type abonnement désactivé");
        }

        boolean needsCne =
                type.getNom() != null &&
                        (type.getNom().equalsIgnoreCase("SCOLAIRE")
                                || type.getNom().toUpperCase().contains("ETUDIANT"));

        if (needsCne) {
            if (cne == null || cne.isBlank()) {
                throw new RuntimeException("CNE obligatoire pour cet abonnement");
            }
        }

        Role role = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Rôle introuvable"));

        User user = new User();
        user.setRole(role);
        user.setNom(dto.getNom());
        user.setPrenom(dto.getPrenom());
        user.setDateNaissance(dto.getDateNaissance());
        user.setTel(dto.getTel());
        user.setEmail(dto.getEmail());
        user.setAdresse(dto.getAdresse());
        user.setCin(cinClean);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setCne(needsCne ? cne : null);

        if (photo != null && !photo.isEmpty())
            user.setPhotoUrl(fileStorageService.savePrivate(photo));

        if (carteScolaire != null && !carteScolaire.isEmpty())
            user.setCarteScolaireUrl(fileStorageService.savePrivate(carteScolaire));

        if (cinPhoto != null && !cinPhoto.isEmpty())
            user.setCinUrl(fileStorageService.savePrivate(cinPhoto));

        User savedUser = userRepository.save(user);
        Dossier dossier = new Dossier();
        dossier.setUser(savedUser);
        dossier.setStatus(StatusDossier.EN_ATTENTE);

        dossierRepository.save(dossier);

        Abonnement abonnement = new Abonnement();
        abonnement.setUser(savedUser);
        abonnement.setTypeAbonnement(type);
        abonnement.setPrix(type.getPrix());
        abonnement.setDateDebut(LocalDateTime.now());
        abonnement.setDateFin(LocalDateTime.now().plusMonths(type.getDureeMois()));
        abonnement.setStatus(StatusAbonnement.EN_ATTENTE);

        abonnementRepository.save(abonnement);
        savedUser.getAbonnements().add(abonnement);

        User userWithAbonnement =
                userRepository.findByIdWithAbonnements(savedUser.getId())
                        .orElseThrow();

        System.out.println("DTO CNE = " + dto.getCne());
        System.out.println("CNE saved = " + savedUser.getCne());
        System.out.println("CNE fetched = " + userWithAbonnement.getCne());
        System.out.println("TYPE = " + type.getNom());
        System.out.println("NEEDS_CNE = " + needsCne);
        return userMapper.toDto(userWithAbonnement);
    }

    private void verifyChamps(UserRequestDto dto,
                              MultipartFile photo,
                              MultipartFile carteScolaire,
                              MultipartFile cinPhoto) {

        if (dto.getNom() == null || dto.getNom().isBlank())
            throw new RuntimeException("Nom obligatoire");

        if (dto.getPrenom() == null || dto.getPrenom().isBlank())
            throw new RuntimeException("Prénom obligatoire");

        if (dto.getEmail() == null || dto.getEmail().isBlank())
            throw new RuntimeException("Email obligatoire");

        if (dto.getPassword() == null || dto.getPassword().isBlank())
            throw new RuntimeException("Mot de passe obligatoire");

        if (dto.getCin() == null || dto.getCin().isBlank())
            throw new RuntimeException("CIN obligatoire");

        if (dto.getTel() == null || dto.getTel().isBlank())
            throw new RuntimeException("Téléphone obligatoire");

        if (dto.getAbonnementId() == null)
            throw new RuntimeException("Type abonnement obligatoire");

        if (photo == null || photo.isEmpty()
                || carteScolaire == null || carteScolaire.isEmpty()
                || cinPhoto == null || cinPhoto.isEmpty()) {
            throw new RuntimeException("Documents obligatoires");
        }
    }

    private void verifyUnicity(UserRequestDto dto,String cinClean,String cne){
        if (userRepository.existsByEmail(dto.getEmail()))
            throw new RuntimeException("Email déjà utilisé");

        if (userRepository.existsByCin(cinClean))
            throw new RuntimeException("CIN déjà utilisé");

        if (userRepository.existsByTel(dto.getTel()))
            throw new RuntimeException("Téléphone déjà utilisé");

        if (cne != null && !cne.isBlank() && userRepository.existsByCne(cne))
            throw new RuntimeException("CNE déjà utilisé");
    }

}
