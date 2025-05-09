package com.tyrdanov.auth_service.exception;

public class UserAlreadyExistAuthenticationException extends RuntimeException {
    public UserAlreadyExistAuthenticationException(String message) {
        super(message);
    }
}
