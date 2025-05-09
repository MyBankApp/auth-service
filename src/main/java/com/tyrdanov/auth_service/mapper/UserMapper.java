package com.tyrdanov.auth_service.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.tyrdanov.auth_service.dto.UpdateUserDto;
import com.tyrdanov.auth_service.dto.UserDto;
import com.tyrdanov.auth_service.model.User;

@Mapper
public interface UserMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "tokens", ignore = true)
    @Mapping(target = "authorities", ignore = true)
    @Mapping(target = "createdDate", ignore = true)
    @Mapping(target = "confirmationCode", ignore = true)
    void update(UpdateUserDto dto, @MappingTarget User user);

    UserDto toDto(User user);

}
