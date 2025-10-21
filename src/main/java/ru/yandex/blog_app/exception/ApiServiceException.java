package ru.yandex.blog_app.exception;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public class ApiServiceException extends RuntimeException {

    private final HttpStatus status;

    public ApiServiceException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
