package com.tyrdanov.auth_service.service;

import java.util.UUID;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyrdanov.auth_service.dto.AuthResponse;
import com.tyrdanov.auth_service.dto.ConfirmEmailRequest;
import com.tyrdanov.auth_service.dto.RefreshRequest;
import com.tyrdanov.auth_service.dto.SignInDto;
import com.tyrdanov.auth_service.dto.SignUpDto;
import com.tyrdanov.auth_service.dto.UserDto;
import com.tyrdanov.auth_service.exception.EmailAlreadyExistException;
import com.tyrdanov.auth_service.exception.ResourceNotFoundException;
import com.tyrdanov.auth_service.exception.UserAlreadyExistAuthenticationException;
import com.tyrdanov.auth_service.exception.UserLogoutException;
import com.tyrdanov.auth_service.mapper.UserMapper;
import com.tyrdanov.auth_service.model.Token;
import com.tyrdanov.auth_service.model.User;
import com.tyrdanov.auth_service.repository.TokenRepository;
import com.tyrdanov.auth_service.repository.UserRepository;
import com.tyrdanov.auth_service.util.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public UserDto register(SignUpDto dto) {
        final var username = dto.getUsername();
        final var password = dto.getPassword();
        final var email = dto.getEmail();
        final var encodedPassword = passwordEncoder.encode(password);
        final var isExistByUsername = userRepository.existsByUsername(username);
        final var isExistByEmail = userRepository.existsByEmail(email);
        final var confirmationCode = UUID.randomUUID().toString();

        if (isExistByUsername) {
            throw new UserAlreadyExistAuthenticationException("Username already exists");
        }

        if (isExistByEmail) {
            throw new EmailAlreadyExistException("Email already exists");
        }

        final var user = User
                .builder()
                .email(email)
                .username(username)
                .password(encodedPassword)
                .confirmationCode(confirmationCode)
                .isConfirmed(false)
                .build();

        final var id = user.getId();
        final var confirmEmailRequest = ConfirmEmailRequest
                .builder()
                .id(id)
                .email(email)
                .confirmationCode(confirmationCode)
                .build();

        final var objectMapper = new ObjectMapper();

        try {
            final var confirmEmailRequestJson = objectMapper.writeValueAsString(confirmEmailRequest);

            kafkaTemplate.send("confirm-email-request-topic", confirmEmailRequestJson);

            final var createdUser = userRepository.save(user);

            return userMapper.toDto(createdUser);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public AuthResponse login(SignInDto dto) {
        final var username = dto.getUsername();
        final var password = dto.getPassword();
        final var authorization = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        SecurityContextHolder.getContext().setAuthentication(authorization);

        final var user = (UserDetails) authorization.getPrincipal();
        final var accessToken = jwtUtil.generateAccessToken(user);
        final var refreshToken = jwtUtil.generateRefreshToken(user);
        final var accessExpiration = jwtUtil.getExpirationFromToken(accessToken);
        final var refreshExpiration = jwtUtil.getExpirationFromToken(refreshToken);
        final var token = Token
                .builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessExpiration(accessExpiration)
                .refreshExpiration(refreshExpiration)
                .user((User) user)
                .build();

        tokenRepository.save(token);

        return new AuthResponse(accessToken, refreshToken);
    }

    @Transactional
    public void logout(HttpServletRequest request) {
        final var header = request.getHeader("Authorization");
        final var isBearer = header != null && header.startsWith("Bearer ");

        if (isBearer) {
            final var accessToken = header.substring(7);

            tokenRepository.deleteByAccessToken(accessToken);

            return;
        }

        throw new UserLogoutException("User is not logged in");
    }

    public UserDto getCurrentUser(UserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }

        final var username = userDetails.getUsername();
        final var user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return userMapper.toDto(user);
    }

    public boolean validate(UserDetails userDetails) {
        return userDetails != null;
    }

    public AuthResponse refresh(RefreshRequest request) {
        final var refreshToken = request.getRefreshToken();
        final var isValidateToken = jwtUtil.validateToken(refreshToken);
        final var token = tokenRepository
                .findByRefreshToken(refreshToken)
                .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

        if (!isValidateToken) {
            tokenRepository.delete(token);

            throw new BadCredentialsException("Refresh token expired");
        }

        final var username = token.getUser().getUsername();
        final var user = userRepository
                .findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        tokenRepository.delete(token);

        final var newAccessToken = jwtUtil.generateAccessToken(user);
        final var newRefreshToken = jwtUtil.generateRefreshToken(user);
        final var accessExpiration = jwtUtil.getExpirationFromToken(newAccessToken);
        final var refreshExpiration = jwtUtil.getExpirationFromToken(newRefreshToken);

        final var newToken = Token
                .builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .accessExpiration(accessExpiration)
                .refreshExpiration(refreshExpiration)
                .user(user)
                .build();

        tokenRepository.save(newToken);

        return new AuthResponse(newAccessToken, newRefreshToken);
    }

}
