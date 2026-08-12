package com.acc.connectauto.exception;

/**
 * Thrown when a requested resource does not exist. Translated to HTTP 404 by
 * {@link GlobalExceptionHandler}.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
