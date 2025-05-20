package com.tyrdanov.auth_service.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import com.tyrdanov.auth_service.enums.Currency;
import com.tyrdanov.auth_service.enums.Status;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TransactionDto {
    
    UUID id;

    BigDecimal amount;

    Currency currency;

    Status status;

    LocalDateTime createdAt;

    String description;

    Long senderId;

    Long receiverId;

    Long categoryId;

}
