package com.securebank.auth.api.error;

import com.securebank.auth.application.exception.EmailAlreadyExistsException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ProblemDetail handleEmailAlreadyExists(
        EmailAlreadyExistsException exception
    ) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT,
            exception.getMessage()
        );

        problem.setTitle("Adresse e-mail déjà utilisée");
        problem.setType(
            URI.create("urn:secure-bank:problem:email-already-exists")
        );

        return problem;
    }

    // Gestion des erreurs de validation
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
        MethodArgumentNotValidException exception,
        HttpHeaders headers,
        HttpStatusCode status,
        WebRequest request
    ) {
        Map<String, List<String>> errors = new LinkedHashMap<>();

        exception.getBindingResult()
            .getFieldErrors()
            .forEach(fieldError -> {
                String field = fieldError.getField();

                String message = fieldError.getDefaultMessage() != null
                    ? fieldError.getDefaultMessage()
                    : "Valeur invalide";

                errors.computeIfAbsent(
                    field,
                    ignored -> new ArrayList<>()
                ).add(message);
            });

        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST,
            "La requête contient des données invalides"
        );

        problem.setTitle("Échec de validation");
        problem.setType(
            URI.create("urn:secure-bank:problem:validation-error")
        );
        problem.setProperty("errors", errors);

        return handleExceptionInternal(
            exception,
            problem,
            headers,
            status,
            request
        );
    }
}
