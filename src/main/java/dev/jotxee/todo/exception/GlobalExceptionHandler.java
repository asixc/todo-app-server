package dev.jotxee.todo.exception;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({EntityNotFoundException.class, EntityAlreadyExistsException.class, EntityWithOutChangesException.class})
    public ResponseEntity<Map<String, String>> handleBusinessException(RuntimeException e) {
        HttpStatus status = switch (e) {
            case EntityNotFoundException _ -> HttpStatus.NOT_FOUND;
            case EntityAlreadyExistsException _ -> HttpStatus.CONFLICT;
            case EntityWithOutChangesException _ -> HttpStatus.UNPROCESSABLE_ENTITY;
            default -> throw new IllegalStateException("Unexpected exception type");
        };

        log.info("Business exception [{}]: {}", status.value(), e.getMessage());
        return ResponseEntity.status(status)
                .body(Map.of("error", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, String>> handleUnexpected(Exception e) {
        log.error("Unexpected error", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Internal server error"));
    }
}
