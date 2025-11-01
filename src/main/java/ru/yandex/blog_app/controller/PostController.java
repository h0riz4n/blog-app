package ru.yandex.blog_app.controller;

import java.net.URI;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.mapper.PostMapper;
import ru.yandex.blog_app.model.dto.Page;
import ru.yandex.blog_app.model.dto.PostDto;
import ru.yandex.blog_app.model.view.PostView;
import ru.yandex.blog_app.service.PostService;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;


@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Контроллер по работе с постами")
@RequestMapping(path = "/api/posts", produces = MediaType.APPLICATION_JSON_VALUE)
public class PostController {

    private final PostMapper postMapper;
    private final PostService postService;

    @PostMapping
    @Operation(summary = "Создание поста")
    public ResponseEntity<PostDto> create(
        @RequestBody @Validated(PostView.Create.class) @JsonView(PostView.Create.class) PostDto dto,
        WebRequest webRequest
    ) {
        var post = postMapper.toDto(postService.create(postMapper.toEntity(dto)));
        return ResponseEntity
            .created(getCreatedUri(webRequest, post.getId()))
            .body(post);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Получение поста по идентификатору")
    public ResponseEntity<PostDto> getById(@PathVariable @NotNull @Positive Long id) {
        return ResponseEntity.ok(postMapper.toDto(postService.getById(id)));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Обновление поста по идентификатору")
    public ResponseEntity<PostDto> update(
        @PathVariable @NotNull @Positive Long id,
        @RequestBody @Validated(PostView.Modify.class) @JsonView(PostView.Modify.class) PostDto dto
    ) {
        return ResponseEntity.ok(postMapper.toDto(postService.updateById(id, postMapper.toEntity(dto))));
    }

    @GetMapping
    @Operation(summary = "Вывод постов постранично")
    public ResponseEntity<Page<PostDto>> getAll(
        @RequestParam String search,
        @RequestParam @NotNull @PositiveOrZero Integer pageNumber,
        @RequestParam @NotNull @Positive Integer pageSize
    ) {
        return ResponseEntity.ok(postMapper.toPage(postService.getAllByTitleAndTags(search, pageNumber, pageSize)));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Удаление поста по идентификатору")
    public ResponseEntity<Void> deleteById(@PathVariable @NotNull @Positive Long id) {
        postService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/likes")
    @Operation(summary = "Лайк поста")
    public ResponseEntity<Long> like(@PathVariable @NotNull @Positive Long id) {
        return ResponseEntity.ok(postService.like(id));
    }

    private URI getCreatedUri(WebRequest request, Long id) {
        return UriComponentsBuilder
            .fromPath("{contextPath}/api/posts/{id}")
            .build(request.getContextPath(), id);
    }
}
