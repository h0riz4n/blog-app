package ru.yandex.blog_app.handler;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.validation.ConstraintViolationException;
import ru.yandex.blog_app.exception.ApiServiceException;
import ru.yandex.blog_app.model.util.MessageResponse;

@RestControllerAdvice
public class RestControllerHandler {

    @ExceptionHandler(ApiServiceException.class)
    public final ResponseEntity<MessageResponse<String>> handleApiServiceException(ApiServiceException e) {
        return ResponseEntity
            .status(e.getStatus())
            .body(new MessageResponse<>("Error", e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<MessageResponse<Map<String, String>>> handleValidationExceptions(MethodArgumentNotValidException e) {
        Map<String, String> errorMap = new HashMap<>();
        e.getBindingResult().getAllErrors().forEach(error -> errorMap.put(((FieldError) error).getField(), error.getDefaultMessage()));
        
        return ResponseEntity
            .badRequest()
            .body(new MessageResponse<>("Error", errorMap));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<MessageResponse<Map<String, String>>> handleConstraintViolationException(ConstraintViolationException e) {
        Map<String, String> errorMap = new HashMap<>();
        e.getConstraintViolations().forEach(error -> errorMap.put(error.getPropertyPath().toString(), error.getMessage()));

        return ResponseEntity
            .badRequest()
            .body(new MessageResponse<>("Error", errorMap));
    }

    @ExceptionHandler(RuntimeException.class)
    public final ResponseEntity<MessageResponse<String>> handleRuntimeException(RuntimeException ex) {
        return ResponseEntity
            .internalServerError()
            .body(new MessageResponse<>("Error", ex.getMessage()));
    }
}
