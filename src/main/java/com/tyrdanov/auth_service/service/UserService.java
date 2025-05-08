package com.tyrdanov.auth_service.service;

import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.tyrdanov.auth_service.dto.UpdateUserDto;
import com.tyrdanov.auth_service.dto.UserDto;
import com.tyrdanov.auth_service.exception.ResourceNotFoundException;
import com.tyrdanov.auth_service.mapper.UserMapper;
import com.tyrdanov.auth_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper mapper;
    private final UserRepository repository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public List<UserDto> getAll() {
        return repository
                .findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public UserDto getById(Long id) {
        final var user = repository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return mapper.toDto(user);
    }

    public UserDetails getByUsername(String username) {
        final var user = repository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        final var password = user.getPassword();

        return new org.springframework.security.core.userdetails.User(
                username,
                password,
                List.of());
    }

    public UserDto update(UpdateUserDto dto) {
        final var id = dto.getId();
        final var password = dto.getPassword();
        final var encodedPassword = bCryptPasswordEncoder.encode(password);
        final var user = repository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        dto.setPassword(encodedPassword);
        mapper.update(dto, user);

        final var savedUser = repository.save(user);

        return mapper.toDto(savedUser);
    }

    public void delete(Long id) {
        repository.deleteById(id);
    }

}
