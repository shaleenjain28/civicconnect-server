package com.civicconnect.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Standardized Error Response sent to the frontend whenever ANY exception occurs.
 * Prevents Spring Boot's ugly default HTML/JSON stack traces from leaking.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String message;
    private String path;
    
    // Used for validation errors (e.g. {"title": "Must not be empty"})
    private Map<String, String> validationErrors;
}
