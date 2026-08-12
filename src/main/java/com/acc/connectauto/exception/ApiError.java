package com.acc.connectauto.exception;

import java.time.Instant;
import java.util.List;

/**
 * Standard error payload returned by the API for any handled exception.
 */
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String message,
        List<String> details) {

    public ApiError(int status, String error, String message) {
        this(Instant.now(), status, error, message, List.of());
    }

    public ApiError(int status, String error, String message, List<String> details) {
        this(Instant.now(), status, error, message, details);
    }
}