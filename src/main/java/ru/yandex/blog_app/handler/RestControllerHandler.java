package ru.yandex.blog_app.handler;

import java.net.URI;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import jakarta.servlet.http.HttpServletRequest;
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

    private ProblemDetail buildProblemDetail(WebRequest webRequest, Exception ex, HttpStatus httpStatus) {
        return buildProblemDetail(webRequest, ex, httpStatus, null);
    }

    private ProblemDetail buildProblemDetail(WebRequest webRequest, Exception ex, HttpStatus httpStatus, Map<String, Object> properties) {
        var uri = URI.create(webRequest.getContextPath());
        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(httpStatus, ex.getMessage());
        problemDetail.setInstance(uri);
        problemDetail.setTitle(httpStatus.getReasonPhrase());
        problemDetail.setProperties(properties);
        return problemDetail;
    }
}
