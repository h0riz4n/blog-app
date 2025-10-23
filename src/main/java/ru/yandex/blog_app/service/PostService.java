package ru.yandex.blog_app.service;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.exception.ApiServiceException;
import ru.yandex.blog_app.model.entity.PostEntity;
import ru.yandex.blog_app.repository.PostRepository;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepo;

    public PostEntity getById(Long id) {
        return postRepo.findById(id)
            .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND, "Пост не найден"));
    }
}
