package com.securebank.transaction.api;

import com.securebank.transaction.exception.IdempotencyConflictException;
import com.securebank.transaction.exception.InvalidIdempotencyKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import com.securebank.transaction.exception.AccountServiceUnavailableException;
import com.securebank.transaction.exception.TransferNotFoundException;
import com.securebank.transaction.exception.TransferRejectedException;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(
        InvalidIdempotencyKeyException.class
    )
    public ResponseEntity<ProblemDetail>
    handleInvalidIdempotencyKey(
        InvalidIdempotencyKeyException exception
    ) {
        ProblemDetail problem =
            ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                exception.getMessage()
            );

        problem.setTitle("Invalid idempotency key");
        problem.setProperty(
            "code",
            "INVALID_IDEMPOTENCY_KEY"
        );

        return ResponseEntity
            .badRequest()
            .body(problem);
    }

    @ExceptionHandler(
        IdempotencyConflictException.class
    )
    public ResponseEntity<ProblemDetail>
    handleIdempotencyConflict(
        IdempotencyConflictException exception
    ) {
        ProblemDetail problem =
            ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                exception.getMessage()
            );

        problem.setTitle("Idempotency conflict");
        problem.setProperty(
            "code",
            "IDEMPOTENCY_KEY_REUSED"
        );

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(problem);
    }

    @ExceptionHandler(TransferRejectedException.class)
    public ResponseEntity<ProblemDetail>
    handleTransferRejected(
        TransferRejectedException exception
    ) {
        ProblemDetail problem =
            ProblemDetail.forStatusAndDetail(
                HttpStatus.UNPROCESSABLE_CONTENT,
                "The transfer could not be completed"
            );

        problem.setTitle("Transfer rejected");
        problem.setProperty(
            "code",
            exception.getFailureCode()
        );

        return ResponseEntity
            .status(HttpStatus.UNPROCESSABLE_CONTENT)
            .body(problem);
    }

    @ExceptionHandler(
        AccountServiceUnavailableException.class
    )
    public ResponseEntity<ProblemDetail>
    handleAccountServiceUnavailable(
        AccountServiceUnavailableException exception
    ) {
        ProblemDetail problem =
            ProblemDetail.forStatusAndDetail(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Account service is temporarily unavailable"
            );

        problem.setTitle("Service unavailable");
        problem.setProperty(
            "code",
            "ACCOUNT_SERVICE_UNAVAILABLE"
        );

        return ResponseEntity
            .status(HttpStatus.SERVICE_UNAVAILABLE)
            .body(problem);
    }

    @ExceptionHandler(TransferNotFoundException.class)
    public ResponseEntity<ProblemDetail>
    handleTransferNotFound(
        TransferNotFoundException exception
    ) {
        ProblemDetail problem =
            ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
            );

        problem.setTitle("Transfer not found");
        problem.setProperty(
            "code",
            "TRANSFER_NOT_FOUND"
        );

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(problem);
    }
}
