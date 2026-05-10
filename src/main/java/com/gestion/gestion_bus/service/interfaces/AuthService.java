package com.gestion.gestion_bus.service.interfaces;

import com.gestion.gestion_bus.dto.request.LoginRequestDto;
import com.gestion.gestion_bus.dto.request.UserRequestDto;
import com.gestion.gestion_bus.dto.response.AuthenticationResponse;
import com.gestion.gestion_bus.dto.response.UserResponseDto;
import org.springframework.web.multipart.MultipartFile;


public interface AuthService {
    AuthenticationResponse login(LoginRequestDto login);
    UserResponseDto registerUser(UserRequestDto user, MultipartFile photo, MultipartFile carteScolaire, MultipartFile cin);

}
