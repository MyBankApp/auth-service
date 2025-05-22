package com.tyrdanov.auth_service.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConfirmEmailRequest {

    Long id;

    String email;

    String confirmationCode;

}
