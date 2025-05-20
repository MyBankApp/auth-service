package com.tyrdanov.auth_service.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {

    Long id;

    String username;

    String email;

    BigDecimal balance = BigDecimal.ZERO;

    LocalDate createdDate;

    String confirmationCode;
    
}
