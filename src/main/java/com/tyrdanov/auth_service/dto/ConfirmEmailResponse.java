package com.tyrdanov.auth_service.dto;

import lombok.Data;

@Data
public class ConfirmEmailResponse {

    Long userId;

    Boolean isConfirmed;

}
