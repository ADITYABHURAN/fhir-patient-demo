package com.example.fhirpatientdemo.exception;

import ca.uhn.fhir.rest.server.exceptions.BaseServerResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Global exception handler for the REST API.
 * 
 * This provides consistent error responses across all endpoints and
 * translates FHIR-specific exceptions into user-friendly messages.
 * 
 * In production healthcare applications, be careful about:
 * - Not exposing internal system details
 * - Logging errors for audit purposes
 * - Providing meaningful error codes for client applications
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Handles validation errors from @Valid annotations.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationErrors(
            MethodArgumentNotValidException ex) {
        
        Map<String, String> fieldErrors = new HashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.put(error.getField(), error.getDefaultMessage());
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.BAD_REQUEST.value());
        response.put("error", "Validation Failed");
        response.put("details", fieldErrors);
        
        return ResponseEntity.badRequest().body(response);
    }

    /**
     * Handles HAPI FHIR client exceptions.
     * 
     * These occur when the FHIR server returns an error (4xx, 5xx)
     * or when there's a communication problem.
     */
    @ExceptionHandler(BaseServerResponseException.class)
    public ResponseEntity<Map<String, Object>> handleFhirException(
            BaseServerResponseException ex) {
        
        log.error("FHIR server error: {} - {}", ex.getStatusCode(), ex.getMessage());
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", ex.getStatusCode());
        response.put("error", "FHIR Server Error");
        response.put("message", ex.getMessage());
        
        return ResponseEntity
                .status(ex.getStatusCode())
                .body(response);
    }

    /**
     * Handles all other unexpected exceptions.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("Unexpected error occurred", ex);
        
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", LocalDateTime.now().toString());
        response.put("status", HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put("error", "Internal Server Error");
        response.put("message", "An unexpected error occurred. Please try again later.");
        
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(response);
    }
}
