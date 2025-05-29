package com.tyrdanov.auth_service.controller.advice;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tyrdanov.auth_service.dto.ErrorResponse;
import com.tyrdanov.auth_service.exception.ResourceNotFoundException;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@ResponseBody
@ControllerAdvice
public class GlobalExceptionControllerAdvice implements AuthenticationEntryPoint {

    // 400: Bad request
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            TypeMismatchException.class,
            HttpMessageNotReadableException.class,
            MethodArgumentNotValidException.class,
            ConstraintViolationException.class
    })
    public ErrorResponse handleBadRequestException(Exception exception, HttpServletRequest request) {
        return handleException(request, exception);
    }

    // 401: Unauthorized
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authenticationException) throws IOException, ServletException {
        final var errorResponse = handleException(request, authenticationException);
        final var jsonResponse = new ObjectMapper().writeValueAsString(errorResponse);

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(jsonResponse);
    }

    // 403: Forbidden
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ExceptionHandler(AccessDeniedException.class)
    public ErrorResponse handleForbiddenException(AccessDeniedException exception, HttpServletRequest request) {
        return handleException(request, exception);
    }

    // 404: Not found
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            ResourceNotFoundException.class,
            NoHandlerFoundException.class
    })
    public ErrorResponse handleNotFoundException(Exception exception, HttpServletRequest request) {
        return handleException(request, exception);
    }

    // 405: Method not allowed
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ErrorResponse handleMethodNotAllowedException(HttpRequestMethodNotSupportedException exception,
            HttpServletRequest request) {
        return handleException(request, exception);
    }

    // 500: Internal server error
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ErrorResponse handleInternalServerException(Exception exception, HttpServletRequest request) {
        return handleException(request, exception);
    }

    private ErrorResponse handleException(HttpServletRequest request, Exception exception) {
        final var currentTime = LocalDateTime.now();
        final var formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss.SSS");
        final var formattedTime = currentTime.format(formatter);
        final var url = request.getRequestURI();
        final var message = exception.getMessage();
        final var user = SecurityContextHolder.getContext().getAuthentication();
        final var username = Optional
                .ofNullable(user)
                .map(Principal::getName)
                .orElse(null);

        return ErrorResponse
                .builder()
                .date(formattedTime)
                .message(message)
                .url(url)
                .user(username)
                .build();
    }

}
