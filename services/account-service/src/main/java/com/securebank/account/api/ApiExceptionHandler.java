package com.securebank.account.api;

import com.securebank.account.exception.AccountNotFoundException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ProblemDetail>
    handleAccountNotFound(
        AccountNotFoundException exception
    ) {

        ProblemDetail problem =
            ProblemDetail.forStatusAndDetail(
                HttpStatus.NOT_FOUND,
                exception.getMessage()
            );

        problem.setTitle("Account not found");
        problem.setProperty(
            "code",
            "ACCOUNT_NOT_FOUND"
        );

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(problem);
    }

    @ExceptionHandler(
        ObjectOptimisticLockingFailureException.class
    )
    public ResponseEntity<ProblemDetail>
    handleConcurrentModification(
        ObjectOptimisticLockingFailureException exception
    ) {

        ProblemDetail problem =
            ProblemDetail.forStatusAndDetail(
                HttpStatus.CONFLICT,
                "The account was modified concurrently. Retry the operation."
            );

        problem.setTitle(
            "Concurrent account modification"
        );
        problem.setProperty(
            "code",
            "ACCOUNT_CONCURRENT_MODIFICATION"
        );

        return ResponseEntity
            .status(HttpStatus.CONFLICT)
            .body(problem);
    }
}
