package com.example.app.domain.common.model;

public class DomainException extends RuntimeException {

	private static final long serialVersionUID = 1077508078763039577L;

	public DomainException(String message) {
		super(message);
	}
	
	public DomainException(String message, Throwable cause) {
		super(message, cause);
	}	
	
}
