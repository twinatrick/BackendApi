package com.example.backedapi.exception;

import com.example.backedapi.model.Vo.ResponseType;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AppException.class)
    public ResponseEntity<ResponseType<?>> handleAppException(AppException ex) {
        ResponseType<?> response = ResponseType.Fail(ex.getErrorType(), ex.getMessage(), ex.getHttpStatus());
        return ResponseEntity.status(ex.getHttpStatus()).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ResponseType<?>> handleIllegalArgument(IllegalArgumentException ex) {
        ResponseType<?> response = ResponseType.Fail("VALIDATION_ERROR", "Invalid request", HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ResponseType<?>> handleNotFound(EntityNotFoundException ex) {
        ResponseType<?> response = ResponseType.Fail("NOT_FOUND", "Not found", HttpStatus.NOT_FOUND.value());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseType<?>> handleException(Exception ex) {
        ResponseType<?> response = ResponseType.Fail("INTERNAL_ERROR", "Internal server error", HttpStatus.INTERNAL_SERVER_ERROR.value());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
