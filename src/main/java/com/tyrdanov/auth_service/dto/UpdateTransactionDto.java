package com.tyrdanov.auth_service.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.tyrdanov.auth_service.enums.Currency;
import com.tyrdanov.auth_service.enums.Status;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UpdateTransactionDto {

    UUID id;

    BigDecimal amount;

    Currency currency;

    Status status;

    String description;

    Long senderId;

    Long receiverId;

    Long categoryId;

}
