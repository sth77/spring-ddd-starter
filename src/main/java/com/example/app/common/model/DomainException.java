package com.example.app.common.model;

/**
 * Base exception for domain-level business rule violations.
 * <p>
 * This exception should be thrown when domain invariants or business rules are violated.
 * HTTP status code mapping is handled by the infrastructure layer (see DomainExceptionHandler).
 */
public class DomainException extends RuntimeException {

	private static final long serialVersionUID = 1077508078763039577L;

	public DomainException(String message) {
		super(message);
	}
	
	public DomainException(String message, Throwable cause) {
		super(message, cause);
	}	
	
}
