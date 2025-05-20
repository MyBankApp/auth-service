package com.tyrdanov.auth_service.exception;

public class TransferFailedException extends RuntimeException {
    public TransferFailedException(String message, Exception exception) {
        super(message, exception);
    }
}
