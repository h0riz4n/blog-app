package ru.yandex.blog_app.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.annotation.JsonView;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.mapper.CommentMapper;
import ru.yandex.blog_app.model.dto.CommentDto;
import ru.yandex.blog_app.model.view.CommentView;
import ru.yandex.blog_app.service.CommentService;
import ru.yandex.blog_app.service.PostService;

@RestController
@Validated
@RequiredArgsConstructor
@Tag(name = "Контроллер по работе с комментариями")
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final PostService postService;

    private final CommentMapper commentMapper;
    private final CommentService commentService;

    @GetMapping
    @Operation(summary = "Получение комментариев поста по идентификатору")
    public ResponseEntity<List<CommentDto>> getAllByPostId(
        @PathVariable("postId") @NotNull @Positive Long postId
    ) {
        return ResponseEntity.ok(commentMapper.toDto(commentService.getAllByPostId(postId)));   
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "Получение комментария по идентификатору")
    public ResponseEntity<CommentDto> getByIdAndPostId(
        @PathVariable("postId") @NotNull @Positive Long postId,
        @PathVariable("commentId") @NotNull @Positive Long commentId
    ) {
        return ResponseEntity.ok(commentMapper.toDto(commentService.getByIdAndPostId(commentId, postId)));
    }

    @PostMapping
    @JsonView(CommentView.Details.class)
    @Operation(summary = "Создание комментария")
    public ResponseEntity<CommentDto> addComment(
        @PathVariable("postId") @NotNull @Positive Long postId,
        @RequestBody @Validated(CommentView.Create.class) @JsonView(CommentView.Create.class) CommentDto commentDto
    ) {
        return ResponseEntity.ok(commentMapper.toDto(commentService.create(postService.getById(postId), commentMapper.toEntity(commentDto))));
    }

    @PutMapping("/{commentId}")
    @Operation(summary = "Обновление комментария")
    public ResponseEntity<CommentDto> update(
        @PathVariable("postId") @NotNull @Positive Long postId,
        @PathVariable("commentId") @NotNull @Positive Long commentId,
        @RequestBody @Validated(CommentView.Modify.class) @JsonView(CommentView.Modify.class) CommentDto commentDto
    ) {
        return ResponseEntity.ok(commentMapper.toDto(commentService.update(postService.getById(postId), commentId, commentMapper.toEntity(commentDto))));
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Удаление комментария")
    public ResponseEntity<Void> delete(
        @PathVariable("postId") @NotNull @Positive Long postId,
        @PathVariable("commentId") @NotNull @Positive Long commentId 
    ) {
        commentService.deleteById(commentId);;
        return ResponseEntity.ok().build();
    }
}
