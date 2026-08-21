package com.testintegrationorg.scriptsign.api;

import com.testintegrationorg.scriptsign.signing.AlreadySignedScriptException;
import com.testintegrationorg.scriptsign.signing.InvalidScriptException;
import com.testintegrationorg.scriptsign.signing.ScriptTooLargeException;
import com.testintegrationorg.scriptsign.signing.SigningConfigurationException;
import com.testintegrationorg.scriptsign.signing.SigningUnavailableException;
import java.net.URI;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ProblemDetail> validationFailure(MethodArgumentNotValidException exception) {
        List<String> fields = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": invalid value")
                .distinct()
                .toList();
        ProblemDetail problem = problem(HttpStatus.BAD_REQUEST, "Invalid signing request", "Request validation failed");
        problem.setProperty("violations", fields);
        return ResponseEntity.badRequest().body(problem);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ProblemDetail> unreadableRequest() {
        return response(HttpStatus.BAD_REQUEST, "Invalid JSON request", "The request body is missing or malformed");
    }

    @ExceptionHandler(InvalidScriptException.class)
    ResponseEntity<ProblemDetail> invalidScript(InvalidScriptException exception) {
        return response(HttpStatus.BAD_REQUEST, "Invalid PowerShell script", exception.getMessage());
    }

    @ExceptionHandler(AlreadySignedScriptException.class)
    ResponseEntity<ProblemDetail> alreadySigned(AlreadySignedScriptException exception) {
        return response(HttpStatus.CONFLICT, "PowerShell script is already signed", exception.getMessage());
    }

    @ExceptionHandler(ScriptTooLargeException.class)
    ResponseEntity<ProblemDetail> scriptTooLarge(ScriptTooLargeException exception) {
        return response(HttpStatus.PAYLOAD_TOO_LARGE, "PowerShell script is too large", exception.getMessage());
    }

    @ExceptionHandler({SigningUnavailableException.class, SigningConfigurationException.class})
    ResponseEntity<ProblemDetail> signingUnavailable(RuntimeException exception) {
        LOGGER.error("Signing request failed; correlationId={}", MDC.get(CorrelationIdFilter.MDC_KEY), exception);
        return response(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Signing service unavailable",
                "The script could not be signed at this time");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> unexpectedFailure(Exception exception) {
        LOGGER.error("Unexpected request failure; correlationId={}", MDC.get(CorrelationIdFilter.MDC_KEY), exception);
        return response(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal server error",
                "The request could not be completed");
    }

    private static ResponseEntity<ProblemDetail> response(HttpStatus status, String title, String detail) {
        return ResponseEntity.status(status).body(problem(status, title, detail));
    }

    private static ProblemDetail problem(HttpStatus status, String title, String detail) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        problem.setType(URI.create("urn:problem:script-sign:" + status.value()));
        String correlationId = MDC.get(CorrelationIdFilter.MDC_KEY);
        if (correlationId != null) {
            problem.setProperty("correlationId", correlationId);
        }
        return problem;
    }
}
