package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.request.UserRequestDto;
import com.gestion.gestion_bus.dto.request.LoginRequestDto;
import com.gestion.gestion_bus.dto.request.UserUpdateDto;
import com.gestion.gestion_bus.dto.response.UserResponseDto;
import com.gestion.gestion_bus.enums.TypeAbonnement;
import com.gestion.gestion_bus.mappers.UserMapper;
import com.gestion.gestion_bus.models.Abonnement;
import com.gestion.gestion_bus.models.ResetPasswordToken;
import com.gestion.gestion_bus.models.Role;
import com.gestion.gestion_bus.models.User;
import com.gestion.gestion_bus.repository.AbonnementRepository;
import com.gestion.gestion_bus.repository.PasswordResetTokenRepository;
import com.gestion.gestion_bus.repository.RoleRepository;
import com.gestion.gestion_bus.repository.UserRepository;
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

    @Override
    public UserResponseDto registerUser(UserRequestDto dto,
                                        MultipartFile photo,
                                        MultipartFile carteScolaire,
                                        MultipartFile cin) {
        if(userRepository.existsByEmail(dto.getEmail())){
            throw new RuntimeException("Email déjà utilisé");
        }
        if(userRepository.existsByCin(dto.getCin())){
            throw new RuntimeException("CIN déjà existante");
        }
        if(userRepository.existsByCne(dto.getCne())){
            throw new RuntimeException("CNE déjà existante");
        }
        if(userRepository.existsByTel(dto.getTel())){
            throw new RuntimeException("Téléphone déjà utilisé");
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
        user.setCin(dto.getCin());
        user.setCne(dto.getCne());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));

        String photoUrl = fileStorageService.save(photo);
        String carteUrl = fileStorageService.save(carteScolaire);
        String cinUrl = fileStorageService.save(cin);

        user.setPhotoUrl(photoUrl);
        user.setCarteScolaireUrl(carteUrl);
        user.setCinUrl(cinUrl);

        User savedUser = userRepository.save(user);

        if (dto.getAbonnement() != null) {
            Abonnement abonnement = new Abonnement();
            try {
                abonnement.setType(TypeAbonnement.valueOf(dto.getAbonnement().toUpperCase()));
            } catch (IllegalArgumentException e) {
                throw new RuntimeException("Type d'abonnement invalide");
            }
            abonnement.setUser(savedUser);
            abonnement.setDateDebut(LocalDate.now());
            abonnement.setDateFin(LocalDate.now().plusMonths(12));
            abonnementRepository.save(abonnement);
            savedUser.setAbonnement(abonnement);
        }

        return userMapper.toDto(savedUser);
    }

    @Override
    public UserResponseDto login(LoginRequestDto login) {
        User user = userRepository.findByEmail(login.getEmail())
                .orElseThrow(() -> new RuntimeException("Email ou mot de passe incorrect"));

        if(!passwordEncoder.matches(login.getPassword(),user.getPassword())){
            throw new RuntimeException("Email ou mot de passe incorrect");
        }
        return userMapper.toDto(user);
    }

    @Override
    public UserResponseDto getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        return userMapper.toDto(user);
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto) {
        User user1 = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Utilisateur introuvable"));
        if (userUpdateDto.getNom() != null) {
            user1.setNom(userUpdateDto.getNom());
        }

        if (userUpdateDto.getPrenom() != null) {
            user1.setPrenom(userUpdateDto.getPrenom());
        }

        if (userUpdateDto.getEmail() != null) {
            user1.setEmail(userUpdateDto.getEmail());
        }

        if (userUpdateDto.getAdresse() != null) {
            user1.setAdresse(userUpdateDto.getAdresse());
        }

        if (userUpdateDto.getDateNaissance() != null) {
            user1.setDateNaissance(userUpdateDto.getDateNaissance());
        }

        if (userUpdateDto.getTel() != null) {
            Optional<User> existingUser = userRepository.findByTel(userUpdateDto.getTel());
            if(existingUser.isPresent() && !existingUser.get().getId().equals(user1.getId())){
                throw new RuntimeException("Ce numéro de téléphone est déjà utilisé");
            }
            user1.setTel(userUpdateDto.getTel());
        }

        User savedUser = userRepository.save(user1);
        return userMapper.toDto(savedUser);
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
        ResetPasswordToken resetPasswordToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Token invalide"));
        if(resetPasswordToken.getExpiryDate().isBefore(LocalDateTime.now())){
            throw new RuntimeException("Token expiré");
        }
        User user = userRepository.findByEmail(resetPasswordToken.getEmail())
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        user.setPassword(passwordEncoder.encode(newPassword));
        passwordResetTokenRepository.delete(resetPasswordToken);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public String forgotPassword(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utilisateur non trouvé"));

        passwordResetTokenRepository.deleteByEmail(email);

        String token = UUID.randomUUID().toString();

        ResetPasswordToken resetPasswordToken = new ResetPasswordToken();
        resetPasswordToken.setEmail(email);
        resetPasswordToken.setToken(token);
        resetPasswordToken.setExpiryDate(
                LocalDateTime.now().plusHours(1)
        );
        passwordResetTokenRepository.save(resetPasswordToken);
        String resetLink = "myapp://reset-password?token=" + token;
        System.out.println("Lien deep link généré: " + resetLink);

        return resetLink;
    }


}