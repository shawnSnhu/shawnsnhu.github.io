package com.shawnbethune.contactservice.api;

import com.shawnbethune.contactservice.exception.ContactNotFoundException;
import com.shawnbethune.contactservice.exception.ContactValidationException;
import com.shawnbethune.contactservice.exception.DuplicateContactException;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(ContactNotFoundException.class)
    public ResponseEntity<ApiError> handleNotFound(ContactNotFoundException exception) {
        return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(DuplicateContactException.class)
    public ResponseEntity<ApiError> handleDuplicate(DuplicateContactException exception) {
        return buildError(HttpStatus.CONFLICT, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(ContactValidationException.class)
    public ResponseEntity<ApiError> handleContactValidation(ContactValidationException exception) {
        return buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), Map.of());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> handleUnreadableRequest(HttpMessageNotReadableException exception) {
        return buildError(HttpStatus.BAD_REQUEST, "Request body is missing or malformed", Map.of());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidation(MethodArgumentNotValidException exception) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        exception.getBindingResult().getFieldErrors().forEach(error ->
                fieldErrors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return buildError(HttpStatus.BAD_REQUEST, "Request validation failed", fieldErrors);
    }

    private ResponseEntity<ApiError> buildError(
            HttpStatus status,
            String message,
            Map<String, String> fieldErrors) {
        ApiError error = new ApiError(
                status.value(),
                status.getReasonPhrase(),
                message,
                fieldErrors);
        return ResponseEntity.status(status).body(error);
    }
}
