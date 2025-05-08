package com.tyrdanov.auth_service.util;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import javax.crypto.SecretKey;
import com.tyrdanov.auth_service.config.JwtConfig;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtConfig config;

    public String generateAccessToken(UserDetails userDetails) {
        final var username = userDetails.getUsername();
        final var accessExpirationMs = config.getAccessExpirationMs();
        final var expirationDate = new Date(System.currentTimeMillis() + accessExpirationMs);
        final var issuedAtDate = new Date();

        return Jwts
                .builder()
                .subject(username)
                .issuedAt(issuedAtDate)
                .expiration(expirationDate)
                .signWith(key())
                .compact();
    }

    public String generateRefreshToken(UserDetails userDetails) {
        final var username = userDetails.getUsername();
        final var refreshExpirationMs = config.getRefreshExpirationMs();
        final var expirationDate = new Date(System.currentTimeMillis() + refreshExpirationMs);
        final var issuedAtDate = new Date();

        return Jwts
                .builder()
                .subject(username)
                .issuedAt(issuedAtDate)
                .expiration(expirationDate)
                .signWith(key())
                .compact();
    }

    public Date getExpirationFromToken(String token) {
        return Jwts
                .parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getExpiration();
    }

    public String getUsername(String token) {
        return Jwts
                .parser()
                .verifyWith(key())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts
                    .parser()
                    .verifyWith(key())
                    .build()
                    .parse(token);

            return true;
        }

        catch (Exception e) {
            return false;
        }
    }

    private SecretKey key() {
        final var secret = config.getSecret();

        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

}
