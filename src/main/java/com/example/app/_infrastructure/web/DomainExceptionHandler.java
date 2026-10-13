package com.example.app._infrastructure.web;

import com.example.app.common.model.AggregateNotFoundException;
import com.example.app.common.model.DomainException;
import com.example.app.common.model.ReferenceDataNotFoundException;
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
 * A DomainException is mapped to 400 Bad Request, as it represents a business rule violation
 * that the client could have prevented (e.g., attempting an operation not allowed in the current
 * aggregate state). The exceptions for an aggregate or reference data item that does not exist are
 * mapped to 404 Not Found; Spring picks the handler of the most specific exception type.
 * <p>
 * This can be extended to handle specific exception subclasses differently:
 * <ul>
 * <li>InvariantViolationException → 500 Internal Server Error (system bug)</li>
 * <li>OptimisticLockException → 409 Conflict</li>
 * <li>etc.</li>
 * </ul>
 */
@ControllerAdvice
public class DomainExceptionHandler {

    @ExceptionHandler(DomainException.class)
    public ResponseEntity<ProblemDetail> handleDomainException(DomainException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler({AggregateNotFoundException.class, ReferenceDataNotFoundException.class})
    public ResponseEntity<ProblemDetail> handleNotFound(DomainException ex) {
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(problemDetail);
    }
}
