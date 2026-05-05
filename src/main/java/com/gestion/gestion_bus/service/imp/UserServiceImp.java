package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.request.UserRequestDto;
import com.gestion.gestion_bus.dto.request.LoginRequestDto;
import com.gestion.gestion_bus.dto.request.UserUpdateDto;
import com.gestion.gestion_bus.dto.response.UserResponseDto;
import com.gestion.gestion_bus.enums.StatusAbonnement;
import com.gestion.gestion_bus.enums.StatusDossier;
import com.gestion.gestion_bus.mappers.UserMapper;
import com.gestion.gestion_bus.models.*;
import com.gestion.gestion_bus.repository.*;
import com.gestion.gestion_bus.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class UserServiceImp implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AbonnementRepository abonnementRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final TypeAbonnementRepository typeAbonnementRepository;
    private final DossierRepository dossierRepository;

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
        user.setPhotoUrl(fileStorageService.save(photo));
        user.setCarteScolaireUrl(fileStorageService.save(carteScolaire));
        user.setCinUrl(fileStorageService.save(cinPhoto));

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

        if (photo == null || carteScolaire == null || cinPhoto == null)
            throw new RuntimeException("Documents obligatoires");
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

    @Override
    public UserResponseDto login(LoginRequestDto login) {
        User user = userRepository.findByEmail(login.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        if (!passwordEncoder.matches(login.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email ou mot de passe incorrect");
        }

        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        return userMapper.toDto(
                userRepository.findById(id)
                        .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"))
        );
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto updateUser(Long id, UserUpdateDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (dto.getNom() != null) user.setNom(dto.getNom());
        if (dto.getPrenom() != null) user.setPrenom(dto.getPrenom());
        if (dto.getEmail() != null) user.setEmail(dto.getEmail());
        if (dto.getAdresse() != null) user.setAdresse(dto.getAdresse());
        if (dto.getDateNaissance() != null) user.setDateNaissance(dto.getDateNaissance());

        if (dto.getTel() != null) {
            Optional<User> existing = userRepository.findByTel(dto.getTel());
            if (existing.isPresent() && !existing.get().getId().equals(user.getId())) {
                throw new RuntimeException("Téléphone déjà utilisé");
            }
            user.setTel(dto.getTel());
        }

        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur introuvable");
        }
        userRepository.deleteById(id);
    }

    @Override
    public void resetPassword(String token, String newPassword) {
        ResetPasswordToken reset = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));

        if (reset.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token expiré");
        }

        User user = userRepository.findByEmail(reset.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setPassword(passwordEncoder.encode(newPassword));

        passwordResetTokenRepository.delete(reset);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        passwordResetTokenRepository.deleteByEmail(email);

        String token = UUID.randomUUID().toString();

        ResetPasswordToken reset = new ResetPasswordToken();
        reset.setEmail(email);
        reset.setToken(token);
        reset.setExpiryDate(LocalDateTime.now().plusHours(1));

        passwordResetTokenRepository.save(reset);

        return "myapp://reset-password?token=" + token;
    }
}