package ru.yandex.blog_app.controller;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.service.PostService;

@Validated
@RestController
@RequiredArgsConstructor
@Tag(name = "Контроллер по работе с изображениями постов")
@RequestMapping(path = "/api/posts/{id}/image")
public class FileController {

    private final PostService postService;

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Обновление изображения поста по идентификатору")
    public ResponseEntity<String> upload(
        @PathVariable("id") @NotNull @Positive Long postId,
        @RequestPart("image") @NotNull MultipartFile file
    ) {
        return ResponseEntity
            .created(UriComponentsBuilder.fromPath("/api/posts/{id}/image").build(postId))
            .body(postService.uploadPostImage(postId, file));
    }

    @GetMapping(produces = { 
        MediaType.IMAGE_PNG_VALUE, 
        MediaType.IMAGE_JPEG_VALUE, 
        MediaType.IMAGE_GIF_VALUE, 
        MediaType.APPLICATION_OCTET_STREAM_VALUE 
    })
    @Operation(summary = "Получение изображения поста по идентификатору")
    public ResponseEntity<byte[]> download(@PathVariable("id") @NotNull @Positive Long postId) {
        return ResponseEntity.ok(postService.downloadPostImage(postId));
    }
}
