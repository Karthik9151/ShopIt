package com.shopit.exception;

import com.shopit.security.TraceIdFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.NoHandlerFoundException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);
    private static final String GENERIC_INTERNAL_MESSAGE = "The request could not be completed";
    private static final String CONFLICT_MESSAGE = "The request conflicts with existing data";

    public record ApiError(
            Instant timestamp,
            int status,
            String error,
            String message,
            Map<String, String> fields,
            String traceId) {}

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> validation(MethodArgumentNotValidException exception) {
        Map<String, String> fields = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors()
                .forEach(error -> fields.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return clientError(HttpStatus.BAD_REQUEST, "Validation failed", fields, exception);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> malformedBody(HttpMessageNotReadableException exception) {
        return clientError(HttpStatus.BAD_REQUEST, "Malformed request body", Map.of(), exception);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiError> invalidParameter(MethodArgumentTypeMismatchException exception) {
        return clientError(HttpStatus.BAD_REQUEST, "Invalid request parameter", Map.of(), exception);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiError> missingParameter(MissingServletRequestParameterException exception) {
        return clientError(HttpStatus.BAD_REQUEST, "Required request parameter is missing", Map.of(), exception);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> constraintViolation(ConstraintViolationException exception) {
        return clientError(HttpStatus.BAD_REQUEST, "Request validation failed", Map.of(), exception);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> methodNotAllowed(HttpRequestMethodNotSupportedException exception) {
        return clientError(HttpStatus.METHOD_NOT_ALLOWED, "Method not allowed", Map.of(), exception);
    }

    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<ApiError> notFound(Exception exception) {
        return clientError(HttpStatus.NOT_FOUND, "Resource not found", Map.of(), exception);
    }

    @ExceptionHandler({DataIntegrityViolationException.class, OptimisticLockingFailureException.class})
    public ResponseEntity<ApiError> conflict(RuntimeException exception) {
        return clientError(HttpStatus.CONFLICT, CONFLICT_MESSAGE, Map.of(), exception);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> forbidden(AccessDeniedException exception) {
        return clientError(HttpStatus.FORBIDDEN, "Access denied", Map.of(), exception);
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<ApiError> status(ResponseStatusException exception) {
        HttpStatus status = HttpStatus.valueOf(exception.getStatusCode().value());
        String message = exception.getReason() == null ? "Request failed" : exception.getReason();
        return clientError(status, message, Map.of(), exception);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> badArgument(IllegalArgumentException exception) {
        return clientError(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of(), exception);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> internal(Exception exception, HttpServletRequest request) {
        String traceId = (String) request.getAttribute(TraceIdFilter.TRACE_ID_ATTRIBUTE);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString().substring(0, 8);
        }
        log.error("Unexpected request failure traceId={}", traceId, exception);
        ApiError body = new ApiError(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                GENERIC_INTERNAL_MESSAGE,
                Map.of(),
                traceId);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    private ResponseEntity<ApiError> clientError(
            HttpStatus status,
            String message,
            Map<String, String> fields,
            Exception exception) {
        log.warn("Handled request error status={} type={}", status.value(), exception.getClass().getSimpleName());
        ApiError body = new ApiError(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message == null ? "Request failed" : message,
                fields,
                null);
        return ResponseEntity.status(status).body(body);
    }
}
