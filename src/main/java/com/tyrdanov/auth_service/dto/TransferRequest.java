package com.tyrdanov.auth_service.dto;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

@Data
public class TransferRequest {

    UUID id;
    
    Long senderId;

    Long receiverId;

    BigDecimal amount;

    String description;

    Long categoryId;
    
}
