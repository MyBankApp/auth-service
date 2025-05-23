package com.tyrdanov.auth_service.exception;

public class LimitOperationsException extends RuntimeException {
    public LimitOperationsException(String message) {
        super(message);
    }
}
