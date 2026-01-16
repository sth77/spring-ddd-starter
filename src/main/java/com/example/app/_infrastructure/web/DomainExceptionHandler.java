package com.example.app._infrastructure.web;

import com.example.app.common.model.DomainException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Infrastructure layer handler that translates domain exceptions to HTTP responses.
 * <p>
 * This follows the onion architecture principle where the infrastructure layer is
 * responsible for translating domain concepts to transport-specific responses.
 * <p>
 * Currently, all DomainExceptions are mapped to 400 Bad Request as they represent
 * business rule violations that the client could have prevented (e.g., attempting
 * operations not allowed in the current aggregate state).
 * <p>
 * This can be extended to handle specific exception subclasses differently:
 * <ul>
 *     <li>InvariantViolationException → 500 Internal Server Error (system bug)</li>
 *     <li>OptimisticLockException → 409 Conflict</li>
 *     <li>etc.</li>
 * </ul>
 */
@ControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(DomainException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                ex.getMessage()
        );
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(problemDetail);
    }
}
