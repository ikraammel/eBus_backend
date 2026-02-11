package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.dto.request.UserRequestDto;
import com.gestion.gestion_bus.dto.request.LoginRequestDto;
import com.gestion.gestion_bus.dto.response.UserResponseDto;
import com.gestion.gestion_bus.enums.TypeAbonnement;
import com.gestion.gestion_bus.mappers.UserMapper;
import com.gestion.gestion_bus.models.Abonnement;
import com.gestion.gestion_bus.models.Role;
import com.gestion.gestion_bus.models.User;
import com.gestion.gestion_bus.repository.AbonnementRepository;
import com.gestion.gestion_bus.repository.RoleRepository;
import com.gestion.gestion_bus.repository.UserRepository;
import com.gestion.gestion_bus.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class IUserService implements UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final AbonnementRepository abonnementRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto registerUser(UserRequestDto dto) {
        if(userRepository.existsByEmail(dto.getEmail())){
            throw new RuntimeException("Email déjà utilisé");
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
        user.setCIN(dto.getCIN());
        user.setCNE(dto.getCNE());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setPhotoUrl(dto.getPhotoUrl());
        user.setCarteScolaireUrl(dto.getCarteScolaireUrl());
        user.setAttestationUrl(dto.getAttestationUrl());

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
    public UserResponseDto updateUser(Long id, UserResponseDto user) {
        return null;
    }

    @Override
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("Utilisateur introuvable");
        }
        userRepository.deleteById(id);
    }
}