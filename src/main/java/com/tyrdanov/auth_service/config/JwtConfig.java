package com.tyrdanov.auth_service.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import lombok.AccessLevel;
import lombok.Data;
import lombok.experimental.FieldDefaults;

@Data
@Configuration
@ConfigurationProperties("jwt")
@FieldDefaults(level = AccessLevel.PRIVATE)
public class JwtConfig {
    
    String secret;

    Long accessExpirationMs;

    Long refreshExpirationMs;

}
