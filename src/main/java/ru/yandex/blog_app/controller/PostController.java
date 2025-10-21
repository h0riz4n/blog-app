package ru.yandex.blog_app.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.mapper.PostMapper;
import ru.yandex.blog_app.model.dto.Page;
import ru.yandex.blog_app.model.dto.PostDto;
import ru.yandex.blog_app.model.view.PostView;
import ru.yandex.blog_app.service.PostService;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/posts", produces = MediaType.APPLICATION_JSON_VALUE)
public class PostController {

    private final PostMapper postMapper;
    private final PostService postService;

    @PostMapping
    @JsonView(PostView.Detail.class)
    public ResponseEntity<PostDto> create(@RequestBody @Validated(PostView.Create.class) @JsonView(PostView.Summary.class) PostDto postDto) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(postMapper.toDto(postService.create(postMapper.toEntity(postDto))));
    }

    @GetMapping
    public ResponseEntity<Page<PostDto>> getAll(
        @RequestParam("search") String search,
        @RequestParam("pageNumber") @NotNull @PositiveOrZero Integer pageNumber,
        @RequestParam("pageSize") @NotNull @Positive Integer pageSize
    ) {
        return ResponseEntity.ok(postMapper.toPage(postService.getAll(search, pageNumber, pageSize)));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<PostDto> getById(@PathVariable("id") @NotNull @Positive Long id) {
        return ResponseEntity.ok(postMapper.toDto(postService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PostDto> updateById(
        @PathVariable("id") Long id,
        @RequestBody @Validated(PostView.Update.class) @JsonView(PostView.Update.class) PostDto dto
    ) {
        return ResponseEntity.ok(postMapper.toDto(postService.updateById(id, postMapper.toEntity(dto))));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(
        @PathVariable("id") @NotNull @Positive Long id
    ) {
        postService.deleteById(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/likes")
    public ResponseEntity<Long> like(
        @PathVariable("id") @NotNull @Positive Long id
    ) {
        return ResponseEntity.ok(postService.like(id).getLikesCount());
    }
}
