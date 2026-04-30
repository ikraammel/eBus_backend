package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.dto.request.LoginRequestDto;
import com.gestion.gestion_bus.dto.request.UserRequestDto;
import com.gestion.gestion_bus.dto.request.UserUpdateDto;
import com.gestion.gestion_bus.dto.response.DossierResponseDto;
import com.gestion.gestion_bus.dto.response.UserResponseDto;
import com.gestion.gestion_bus.service.interfaces.DossierService;
import com.gestion.gestion_bus.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final DossierService dossierService;

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDto> register(
            @RequestPart("user") String userJson,
            @RequestPart("photo") MultipartFile photo,
            @RequestPart("carteScolaire") MultipartFile carteScolaire,
            @RequestPart("cinPhoto") MultipartFile cinPhoto
    ) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        UserRequestDto dto = mapper.readValue(userJson, UserRequestDto.class);

        return ResponseEntity.ok(userService.registerUser(dto, photo, carteScolaire, cinPhoto));
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<UserResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDto dto ){
        return ResponseEntity.ok(userService.updateUser(id,dto));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> login(@RequestBody LoginRequestDto dto){
        return ResponseEntity.ok(userService.login(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email){
        String resetLink = userService.forgotPassword(email);
        return ResponseEntity.ok("Lien de réinitialisation généré : " + resetLink);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestParam String token,@RequestParam String newPassword){
        userService.resetPassword(token,newPassword);
        return ResponseEntity.ok("Mot de passe rénitialisé avec succès!");
    }

    @GetMapping("/me/dossier")
    public DossierResponseDto getMyDossier(@RequestParam Long id) {
        return dossierService.getMyDossier(id);
    }
}
