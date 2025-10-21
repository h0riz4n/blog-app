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

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.mapper.CommentMapper;
import ru.yandex.blog_app.model.dto.CommentDto;
import ru.yandex.blog_app.model.view.CommentView;
import ru.yandex.blog_app.service.CommentService;
import ru.yandex.blog_app.service.PostService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts/{postId}/comments")
public class CommentController {

    private final PostService postService;

    private final CommentMapper commentMapper;
    private final CommentService commentService;

    @GetMapping
    public ResponseEntity<List<CommentDto>> getAllByPostId(
        @PathVariable("postId") @NotNull @Positive Long postId
    ) {
        return ResponseEntity.ok(commentMapper.toDto(commentService.getAllByPostId(postId)));   
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<CommentDto> getByIdAndPostId(
        @PathVariable("postId") @NotNull @Positive Long postId,
        @PathVariable("commentId") @NotNull @Positive Long commentId
    ) {
        return ResponseEntity.ok(commentMapper.toDto(commentService.getByIdAndPostId(commentId, postId)));
    }

    @PostMapping
    @JsonView(CommentView.Details.class)
    public ResponseEntity<CommentDto> addComment(
        @PathVariable("postId") @NotNull @Positive Long postId,
        @RequestBody @Validated(CommentView.Create.class) @JsonView(CommentView.Create.class) CommentDto commentDto
    ) {
        postService.getById(postId); // verify post
        return ResponseEntity.ok(commentMapper.toDto(commentService.addComment(postId, commentMapper.toEntity(commentDto))));
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<CommentDto> update(
        @PathVariable("postId") @NotNull @Positive Long postId,
        @PathVariable("commentId") @NotNull @Positive Long commentId,
        @RequestBody @Validated(CommentView.Update.class) @JsonView(CommentView.Update.class) CommentDto commentDto
    ) {
        return ResponseEntity.ok(commentMapper.toDto(commentService.updateComment(postId, commentId, commentMapper.toEntity(commentDto))));
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<Void> delete(
        @PathVariable("postId") @NotNull @Positive Long postId,
        @PathVariable("commentId") @NotNull @Positive Long commentId 
    ) {
        commentService.deleteByIdAndPostId(commentId, postId);
        return ResponseEntity.ok().build();
    }
}
