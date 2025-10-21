package ru.yandex.blog_app.dao;

import java.util.List;
import java.util.Optional;

import ru.yandex.blog_app.model.domain.Post;
import ru.yandex.blog_app.model.dto.Page;

public interface PostDao {

    Post save(Post post);

    Optional<Post> findById(Long id);

    Page<Post> findAll(String search, List<String> tagsText, Integer pageNumber, Integer pageSize);

    void updateText(Long id, String text);

    void updateLikesCount(Long id, Long likesCount);

    void deleteById(Long id);

    void updateFileName(Long id, String fileName);

    Optional<String> findFileNameById(Long id);
}
