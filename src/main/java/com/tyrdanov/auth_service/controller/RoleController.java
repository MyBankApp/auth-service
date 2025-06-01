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

import com.tyrdanov.auth_service.dto.RoleDto;
import com.tyrdanov.auth_service.dto.UpdateRoleDto;
import com.tyrdanov.auth_service.service.RoleService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService service;

    @GetMapping
    public List<RoleDto> getAll() {
        return service.getAll();
    }

    @GetMapping("{id}")
    public RoleDto getById(@PathVariable Long id) {
        return service.getById(id);
    }

    @PostMapping
    public RoleDto create(@RequestBody RoleDto dto) {
        return service.create(dto);
    }

    @PutMapping
    public UpdateRoleDto update(@RequestBody UpdateRoleDto dto) {
        return service.update(dto);
    }

    @DeleteMapping("{id}")
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
    
}
