package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.dto.ChangePasswordDto;
import com.gestion.gestion_bus.dto.request.LoginRequestDto;
import com.gestion.gestion_bus.dto.request.UserRequestDto;
import com.gestion.gestion_bus.dto.request.UserUpdateDto;
import com.gestion.gestion_bus.dto.response.ApiResponse;
import com.gestion.gestion_bus.dto.response.AuthenticationResponse;
import com.gestion.gestion_bus.dto.response.DossierResponseDto;
import com.gestion.gestion_bus.dto.response.UserResponseDto;
import com.gestion.gestion_bus.models.User;
import com.gestion.gestion_bus.repository.UserRepository;
import com.gestion.gestion_bus.service.imp.EmailServiceImp;
import com.gestion.gestion_bus.service.interfaces.AuthService;
import com.gestion.gestion_bus.service.interfaces.DossierService;
import com.gestion.gestion_bus.service.interfaces.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final AuthService authService;
    private final DossierService dossierService;
    private final EmailServiceImp emailService;
    private final UserRepository userRepository;
    private final Map<String, String> resetCodes = new HashMap<>();
    private final Map<String, String> resetTokens = new HashMap<>();

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDto> register(
            @RequestPart("user") String userJson,
            @RequestPart(value = "photo", required = false) MultipartFile photo,
            @RequestPart(value = "carteScolaire", required = false) MultipartFile carteScolaire,
            @RequestPart(value = "attestationScolaire", required = false) MultipartFile attestationScolaire,
            @RequestPart(value = "cinPhoto", required = false) MultipartFile cinPhoto
    ) throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        UserRequestDto dto = mapper.readValue(userJson, UserRequestDto.class);
        System.out.println("photo = " + (photo != null));
        System.out.println("carteScolaire = " + (carteScolaire != null));
        System.out.println("cinPhoto = " + (cinPhoto != null));
        System.out.println("attestationScolaire = " + (attestationScolaire != null));

        return ResponseEntity.ok(authService.registerUser(dto, photo, carteScolaire, attestationScolaire, cinPhoto));
    }

    @PatchMapping("/update/{id}")
    public ResponseEntity<UserResponseDto> update(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateDto dto ){
        return ResponseEntity.ok(userService.updateUser(id,dto));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody LoginRequestDto dto){
        return ResponseEntity.ok(authService.login(dto));
    }

    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id){
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserResponseDto>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id){
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-reset-code")
    public ResponseEntity<ApiResponse<Map<String, String>>> verifyCode(
            @RequestBody Map<String, String> body) {

        String email = body.get("email");
        String code = body.get("code");

        String savedCode = resetCodes.get(email);

        if (savedCode == null || !savedCode.equals(code)) {
            return ResponseEntity
                    .status(400)
                    .body(ApiResponse.error("Code invalide"));
        }

        String token = UUID.randomUUID().toString();
        resetTokens.put(token, email);

        return ResponseEntity.ok(
                ApiResponse.ok(Map.of("token", token),
                        "Code vérifié")
        );
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(
            @RequestBody Map<String, String> body) {

        String email = body.get("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Aucun compte"));

        String code = String.valueOf((int)(Math.random() * 900000) + 100000);

        resetCodes.put(email, code);

        emailService.sendEmail(
                email,
                "Code de réinitialisation",
                "Votre code est : " + code
        );

        return ResponseEntity.ok(
                ApiResponse.ok(null, "Code envoyé")
        );
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @RequestBody Map<String, String> body) {

        String token = body.get("token");
        String newPassword = body.get("newPassword");

        if (token == null || newPassword == null) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Token ou mot de passe manquant"));
        }

        String email = resetTokens.get(token);

        if (email == null) {
            return ResponseEntity.status(400)
                    .body(ApiResponse.error("Token invalide"));
        }

        userService.resetPasswordByEmail(email, newPassword);

        resetTokens.remove(token); // usage unique

        return ResponseEntity.ok(
                ApiResponse.ok(null, "Mot de passe réinitialisé avec succès")
        );
    }

    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @GetMapping("/me/dossier")
    public DossierResponseDto getMyDossier(@RequestParam Long id) {
        return dossierService.getMyDossier(id);
    }

    @PreAuthorize("#id == authentication.principal.id or hasRole('ADMIN')")
    @PatchMapping(value = "/update-avatar/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDto> updateAvatar(
            @PathVariable Long id,
            @RequestPart(value = "photo") MultipartFile photo) {

        return ResponseEntity.ok(userService.updateAvatar(id, photo));
    }

    @PatchMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody ChangePasswordDto dto
    ){
        userService.changePassword(dto.userId,dto.oldPassword,dto.newPassword);
        return ResponseEntity.ok(ApiResponse.ok(null, "Mot de passe modifié avec succès"));
    }


    @PatchMapping(value = "/resubmit-dossier/{userId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDto> resubmitDossier(
            @PathVariable Long userId,
            @RequestPart(value = "cinPhoto", required = false) MultipartFile cinPhoto,
            @RequestPart(value = "carteScolaire", required = false) MultipartFile carteScolaire,
            @RequestPart(value = "attestationScolaire", required = false) MultipartFile attestationScolaire,
            @RequestPart(value = "photo", required = false) MultipartFile photo
            ) {
        return ResponseEntity.ok(userService.resubmitDossier(userId, cinPhoto, carteScolaire, attestationScolaire,photo));
    }

}
