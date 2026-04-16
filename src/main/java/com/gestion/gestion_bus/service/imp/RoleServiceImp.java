package com.gestion.gestion_bus.service.imp;

import com.gestion.gestion_bus.models.Role;
import com.gestion.gestion_bus.models.User;
import com.gestion.gestion_bus.repository.RoleRepository;
import com.gestion.gestion_bus.repository.UserRepository;
import com.gestion.gestion_bus.service.interfaces.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class RoleServiceImp implements RoleService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public void changeRole(Long userId, String roleName) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User introuvable"));

        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new RuntimeException("Role introuvable"));

        user.setRole(role);
    }
}
