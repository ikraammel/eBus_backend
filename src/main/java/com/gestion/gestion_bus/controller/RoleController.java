package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.service.interfaces.RoleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/role")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{userId}")
    public ResponseEntity<Void> changeRole(
            @PathVariable Long userId,
            @RequestParam String roleName
    ) {
        roleService.changeRole(userId, roleName);
        return ResponseEntity.ok().build();
    }
}
