package com.tyrdanov.auth_service.controller;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.tyrdanov.auth_service.dto.ConfirmRegistrationDto;
import com.tyrdanov.auth_service.dto.TransferRequest;
import com.tyrdanov.auth_service.dto.UpdateUserDto;
import com.tyrdanov.auth_service.dto.UserDto;
import com.tyrdanov.auth_service.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {
    
    private final UserService service;

    @GetMapping
    public List<UserDto> getAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public UserDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @GetMapping("/register/{generatedString}")
    public ConfirmRegistrationDto confirmRegistration(@PathVariable String generatedString) {
        return service.confirmRegistration(generatedString);
    }

    @PostMapping
    public void makeTransfer(@RequestBody TransferRequest request) {
        service.makeTransfer(request);
    }

    @PutMapping
    public UserDto update(@RequestBody UpdateUserDto dto) {
        return service.update(dto);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }

}
