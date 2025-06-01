package com.tyrdanov.auth_service.dto;

import java.util.List;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RoleDto {
    
    Long id;

    String name;

    List<Long> userIds;

}
