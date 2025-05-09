package com.tyrdanov.auth_service.repository;

import java.util.Date;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.tyrdanov.auth_service.model.Token;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {

    Optional<Token> findByAccessToken(String accessToken);

    Optional<Token> findByRefreshToken(String refreshToken);

    void deleteByAccessToken(String accessToken);

    void deleteByAccessExpirationBefore(Date date);

    void deleteByRefreshExpirationBefore(Date date);
    
}
