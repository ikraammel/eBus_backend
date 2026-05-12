package com.gestion.gestion_bus.controller;

import com.gestion.gestion_bus.dto.response.UserResponseDto;
import com.gestion.gestion_bus.service.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/test")
@RequiredArgsConstructor
public class TestController {

    private final UserService userService;

    @GetMapping("/user/{id}")
    public UserResponseDto testUserResponse(@PathVariable Long id) {
        return userService.getUserById(id);
    }
}
