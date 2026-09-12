package com.securebank.audit.api;

import com.securebank.audit.exception.AuditEventNotFoundException;
import com.securebank.audit.exception.InvalidAuditSearchException;
import com.securebank.audit.exception.InvalidInternalCredentialsException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(
        InvalidInternalCredentialsException.class
    )
    public ResponseEntity<ProblemDetail>
    handleInvalidInternalCredentials(
        InvalidInternalCredentialsException exception
    ) {
        return createProblem(
            HttpStatus.UNAUTHORIZED,
            "Unauthorized",
            "INVALID_INTERNAL_CREDENTIALS",
            exception.getMessage()
        );
    }

    @ExceptionHandler(
        AuditEventNotFoundException.class
    )
    public ResponseEntity<ProblemDetail>
    handleAuditEventNotFound(
        AuditEventNotFoundException exception
    ) {
        return createProblem(
            HttpStatus.NOT_FOUND,
            "Audit event not found",
            "AUDIT_EVENT_NOT_FOUND",
            exception.getMessage()
        );
    }

    @ExceptionHandler(
        InvalidAuditSearchException.class
    )
    public ResponseEntity<ProblemDetail>
    handleInvalidSearch(
        InvalidAuditSearchException exception
    ) {
        return createProblem(
            HttpStatus.BAD_REQUEST,
            "Invalid audit search",
            "INVALID_AUDIT_SEARCH",
            exception.getMessage()
        );
    }

    private ResponseEntity<ProblemDetail> createProblem(
        HttpStatus status,
        String title,
        String code,
        String detail
    ) {
        ProblemDetail problem =
            ProblemDetail.forStatusAndDetail(
                status,
                detail
            );

        problem.setTitle(title);
        problem.setProperty("code", code);

        return ResponseEntity
            .status(status)
            .body(problem);
    }
}
