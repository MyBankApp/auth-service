package com.tyrdanov.auth_service.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tyrdanov.auth_service.dto.AuthResponse;
import com.tyrdanov.auth_service.dto.RefreshRequest;
import com.tyrdanov.auth_service.dto.SignInDto;
import com.tyrdanov.auth_service.dto.SignUpDto;
import com.tyrdanov.auth_service.dto.UserDto;
import com.tyrdanov.auth_service.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth/")
public class AuthController {

    private final AuthService service;
    
    @PostMapping("/register")
    public UserDto register(@RequestBody SignUpDto dto) {
        return service.register(dto);
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody SignInDto dto) {
        return service.login(dto);
    }

    @PostMapping("/logout")
    public void logout(HttpServletRequest request) {
        service.logout(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestBody RefreshRequest request) {
        return service.refresh(request);
    }

    @GetMapping("/me")
    public UserDto getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return service.getCurrentUser(userDetails);
    }

    @GetMapping("/validate")
    public boolean validateToken(@AuthenticationPrincipal UserDetails userDetails) {
        return service.validate(userDetails);
    }

}
