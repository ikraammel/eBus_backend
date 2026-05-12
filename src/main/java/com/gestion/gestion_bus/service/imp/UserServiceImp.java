package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.request.UserRequestDto;
import com.gestion.gestion_bus.dto.request.UserUpdateDto;
import com.gestion.gestion_bus.dto.response.UserResponseDto;
import com.gestion.gestion_bus.enums.StatusAbonnement;
import com.gestion.gestion_bus.enums.StatusDossier;
import com.gestion.gestion_bus.mappers.UserMapper;
import com.gestion.gestion_bus.models.*;
import com.gestion.gestion_bus.repository.*;
import com.gestion.gestion_bus.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final FileStorageService fileStorageService;
    private final TypeAbonnementRepository typeAbonnementRepository;
    private final DossierRepository dossierRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final EmailServiceImp emailService;

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
    @Transactional
    public void resetPassword(String token, String newPassword) {

        PasswordResetToken prt = tokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Code incorrect"));

        if (prt.isUsed() || prt.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expiré");
        }

        User user = userRepository.findById(prt.getUserId())
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        prt.setUsed(true);
        tokenRepository.save(prt);
    }


    @Transactional
    @Override
    public void requestPasswordReset(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte associé à cet email"));

        String token = UUID.randomUUID().toString();

        PasswordResetToken prt = new PasswordResetToken();
        prt.setToken(token);
        prt.setUserId(user.getId());
        prt.setExpiresAt(LocalDateTime.now().plusMinutes(15));
        prt.setUsed(false);

        tokenRepository.save(prt);

        emailService.sendEmail(
                user.getEmail(),
                "Reset Password",
                "Votre code de réinitialisation : " + token
        );

//        log.info("Reset email sent to {}", email);
    }

    @Override
    public void resetPasswordByEmail(String email, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserResponseDto updateAvatar(Long id, MultipartFile photo) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        if (photo == null || photo.isEmpty()) {
            throw new RuntimeException("Aucune photo fournie");
        }

        String photoUrl = fileStorageService.save(photo);
        user.setPhotoUrl(photoUrl);
        userRepository.save(user);

        return userMapper.toDto(user);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())){
            throw new RuntimeException("Ancien mot de passe incorrect");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public UserResponseDto resubmitDossier(Long userId, MultipartFile newCinFile, MultipartFile newCarteScolaireFile) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));

        // Mettre à jour la photo CIN si fournie
        if (newCinFile != null && !newCinFile.isEmpty()) {
            String cinUrl = fileStorageService.save(newCinFile);
            user.setCinUrl(cinUrl);
        }

        // Mettre à jour la carte scolaire si fournie
        if (newCarteScolaireFile != null && !newCarteScolaireFile.isEmpty()) {
            String carteScolaireUrl = fileStorageService.save(newCarteScolaireFile);
            user.setCarteScolaireUrl(carteScolaireUrl);
        }

        userRepository.save(user);

        // Remettre le dossier à EN_ATTENTE
        Dossier dossier = dossierRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Dossier introuvable"));

        dossier.setStatus(StatusDossier.EN_ATTENTE);
        dossier.setRejectionReason(null);
        dossier.setUpdatedAt(java.time.LocalDateTime.now());
        dossierRepository.save(dossier);

        return userMapper.toDto(user);
    }
}