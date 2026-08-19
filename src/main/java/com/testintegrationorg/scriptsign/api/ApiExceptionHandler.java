package com.testintegrationorg.scriptsign.api;

import com.testintegrationorg.scriptsign.signing.SigningException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(new ApiError("INVALID_REQUEST", "Request validation failed", Instant.now()));
    }

    @ExceptionHandler(SigningException.class)
    ResponseEntity<ApiError> handleSigning(SigningException ex) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(new ApiError("SIGNING_FAILED", ex.getMessage(), Instant.now()));
    }

    record ApiError(String code, String message, Instant timestamp) {
    }
}
