package ru.yandex.blog_app.handler;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import lombok.extern.slf4j.Slf4j;
import ru.yandex.blog_app.exception.ApiServiceException;

@Slf4j
@RestControllerAdvice
public class RestControllerHandler extends ResponseEntityExceptionHandler  {

    @ExceptionHandler(ApiServiceException.class)
    public final ResponseEntity<ProblemDetail> handleApiServiceException(ApiServiceException ex, WebRequest webRequest) {
        return ResponseEntity
            .status(ex.getStatus())
            .body(buildProblemDetail(webRequest, ex, ex.getStatus()));
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest webRequest) {
        Map<String, String> errorMap = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> errorMap.put(((FieldError) error).getField(), error.getDefaultMessage()));
        ProblemDetail body = buildProblemDetail(webRequest, ex, HttpStatus.BAD_REQUEST, Map.of("validation", errorMap));
        return this.handleExceptionInternal(ex, body, headers, status, webRequest);
    }


    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatusCode statusCode, WebRequest request) {
        log.debug(ex.getMessage());
        return super.handleExceptionInternal(ex, body, headers, statusCode, request);
    }

    private ProblemDetail buildProblemDetail(WebRequest webRequest, Exception ex, HttpStatus httpStatus) {
        return buildProblemDetail(webRequest, ex, httpStatus, null);
    }

    private ProblemDetail buildProblemDetail(WebRequest webRequest, Exception ex, HttpStatus httpStatus, Map<String, Object> properties) {
        var uri = URI.create(webRequest.getContextPath());
        var problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, ex.getMessage());
        problemDetail.setInstance(uri);
        problemDetail.setTitle(httpStatus.getReasonPhrase());
        problemDetail.setProperties(properties);
        return problemDetail;
    }
}
