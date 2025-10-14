package ru.yandex.blog_app.service;

import org.springframework.web.multipart.MultipartFile;

import ru.yandex.blog_app.model.domain.Post;
import ru.yandex.blog_app.model.dto.Page;

public interface PostService {

    Post create(Post post);

    Page<Post> getAll(String search, Integer pageNumber, Integer pageSize);

    Post getById(Long id);

    Post updateById(Long id, Post newPost);

    void deleteById(Long id);

    Post like(Long id);

    String uploadPostImage(Long id, MultipartFile file);

    byte[] downloadPostImage(Long id);
}
