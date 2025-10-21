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

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.service.PostService;

@Validated
@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/api/posts/{postId}/image")
public class FileController {

    private final PostService postService;

    @PutMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> upload(
        @PathVariable("postId") @NotNull @Positive Long postId,
        @RequestPart("image") @NotNull MultipartFile file
    ) {
        return ResponseEntity.ok(postService.uploadPostImage(postId, file));
    }

    @GetMapping
    public ResponseEntity<byte[]> download(
        @PathVariable("postId") @NotNull @Positive Long postId
    ) {
        return ResponseEntity.ok(postService.downloadPostImage(postId));
    }
}
