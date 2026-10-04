package com.giovanni.similarproducts.exception;

import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.RestClientException;

import jakarta.servlet.http.HttpServletRequest;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final ZoneId MADRID_ZONE = ZoneId.of("Europe/Madrid");

    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleNotFound(ProductNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    @ExceptionHandler(RestClientException.class)
    public ResponseEntity<ErrorResponseDto> handleUpstream(RestClientException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_GATEWAY, "External API unavailable: " + ex.getMessage(), request);
    }

    private ResponseEntity<ErrorResponseDto> build(HttpStatus status, String message, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ErrorResponseDto(
                LocalDateTime.now(MADRID_ZONE),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI()));
    }
}
