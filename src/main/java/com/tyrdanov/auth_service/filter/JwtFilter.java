package com.tyrdanov.auth_service.filter;

import java.io.IOException;
import java.util.Date;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.tyrdanov.auth_service.repository.TokenRepository;
import com.tyrdanov.auth_service.service.UserDetailsServiceImpl;
import com.tyrdanov.auth_service.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final TokenRepository tokenRepository;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        final var token = extractToken(request);

        if (token != null) {
            final var isValid = tokenRepository
                    .findByAccessToken(token)
                    .map(t -> jwtUtil.validateToken(token) && t.getAccessExpiration().after(new Date()))
                    .orElse(false);

            if (Boolean.TRUE.equals(isValid)) {
                final var username = jwtUtil.getUsername(token);
                final var userDetails = userDetailsService.loadUserByUsername(username);
                final var authorities = userDetails.getAuthorities();
                final var authorization = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
    
                SecurityContextHolder.getContext().setAuthentication(authorization);
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        final var header = request.getHeader("Authorization");
        final var isBearer = header != null && header.startsWith("Bearer ");

        if (isBearer) {
            return header.substring(7);
        }

        return null;
    }

}
