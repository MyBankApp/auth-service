package com.tyrdanov.auth_service.exception;

public class UserLogoutException extends RuntimeException {
    public UserLogoutException(String message) {
        super(message);
    }
}
