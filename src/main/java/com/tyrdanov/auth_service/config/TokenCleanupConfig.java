package com.tyrdanov.auth_service.config;

import java.util.Date;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.tyrdanov.auth_service.repository.TokenRepository;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class TokenCleanupConfig {
    
    private final TokenRepository repository;

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000)
    public void cleanupExpiredTokens() {
        final var now = new Date();

        repository.deleteByAccessExpirationBefore(now);
        repository.deleteByRefreshExpirationBefore(now);
    }

}
