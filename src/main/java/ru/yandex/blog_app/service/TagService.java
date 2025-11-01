package ru.yandex.blog_app.service;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.model.entity.PostEntity;
import ru.yandex.blog_app.model.entity.TagEntity;
import ru.yandex.blog_app.repository.TagRepository;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepo;

    public List<TagEntity> getAllByPostIn(List<PostEntity> posts) {
        return tagRepo.findAllByPostIn(posts);
    }
}
