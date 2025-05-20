package com.tyrdanov.auth_service.dto;

import java.math.BigDecimal;

import com.tyrdanov.auth_service.enums.Currency;
import com.tyrdanov.auth_service.enums.Status;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CreateTransactionDto {
    
    BigDecimal amount;

    Currency currency;

    @Builder.Default
    Status status = Status.PENDING;

    String description;

    Long senderId;

    Long receiverId;

    Long categoryId;

}
