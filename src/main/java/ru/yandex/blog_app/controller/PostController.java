package ru.yandex.blog_app.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.service.PostService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/posts", produces = MediaType.APPLICATION_JSON_VALUE)
public class PostController {
    
    private final PostService postService;

    @GetMapping("/{id}")
    public ResponseEntity<?> getMethodName(@PathVariable("id") Long id) {
        return ResponseEntity.ok(postService.getById(id));
    }
}
