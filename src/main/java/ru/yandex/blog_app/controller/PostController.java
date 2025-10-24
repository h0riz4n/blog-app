package ru.yandex.blog_app.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonView;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.mapper.PostMapper;
import ru.yandex.blog_app.model.dto.PostDto;
import ru.yandex.blog_app.model.view.PostView;
import ru.yandex.blog_app.service.PostService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/posts", produces = MediaType.APPLICATION_JSON_VALUE)
public class PostController {

    private final PostMapper postMapper;
    private final PostService postService;

    @PostMapping
    @JsonView(PostView.Details.class)
    public ResponseEntity<PostDto> create(
        @RequestBody @Validated(PostView.Create.class) @JsonView(PostView.Create.class) PostDto post
    ) {
        PostDto createdPost = postMapper.toDto(postService.create(postMapper.toEntity(post)));
        return ResponseEntity
            .created(UriComponentsBuilder.fromPath("/api/posts/{id}").build(createdPost.getId()))
            .body(createdPost);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getById(@PathVariable @NotNull @Positive Long id) {
        return ResponseEntity.ok(postMapper.toDto(postService.getById(id)));
    }
}
