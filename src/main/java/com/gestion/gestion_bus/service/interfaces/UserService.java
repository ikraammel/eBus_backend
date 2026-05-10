package com.gestion.gestion_bus.service.interfaces;

import com.gestion.gestion_bus.dto.request.UserUpdateDto;
import com.gestion.gestion_bus.dto.response.UserResponseDto;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserService {
    UserResponseDto getUserById(Long id);
    List<UserResponseDto> getAllUsers();
    UserResponseDto updateUser(Long id, UserUpdateDto userUpdateDto);
    void deleteUser(Long id);
    void resetPassword(String token,String newPassword);

    @Transactional
    void requestPasswordReset(String email);

    void resetPasswordByEmail(String email, String newPassword);
    UserResponseDto updateAvatar(Long id, MultipartFile photo);

    void changePassword(Long userId, String oldPassword, String newPassword);
}
