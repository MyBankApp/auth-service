package com.tyrdanov.auth_service.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.tyrdanov.auth_service.dto.RoleDto;
import com.tyrdanov.auth_service.dto.UpdateRoleDto;
import com.tyrdanov.auth_service.exception.ResourceNotFoundException;
import com.tyrdanov.auth_service.mapper.RoleMapper;
import com.tyrdanov.auth_service.repository.RoleRepository;
import com.tyrdanov.auth_service.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {
    
    private final RoleMapper mapper;
    private final RoleRepository repository;
    private final UserRepository userRepository;

    public List<RoleDto> getAll() {
        return repository
                .findAll()
                .stream()
                .map(mapper::toDto)
                .toList();
    }

    public RoleDto getById(Long id) {
        return mapper.toDto(repository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found")));
    }

     public RoleDto create(RoleDto dto) {
        final var ids = dto.getUserIds();
        final var users = userRepository.findAllById(ids);
        final var role = mapper.toEntity(dto, users);
        final var savedRole = repository.save(role);

        return mapper.toDto(savedRole);
    }

    public UpdateRoleDto update(UpdateRoleDto dto) {
        final var id = dto.getId();
        final var role = repository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found"));

        mapper.update(dto, role);

        repository.save(role);

        return dto;
    }

    

    public void delete(Long id) {
        repository.deleteById(id);
    }

}
