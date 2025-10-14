package ru.yandex.blog_app.dao;

import java.util.List;
import java.util.Optional;

import ru.yandex.blog_app.model.domain.Comment;

public interface CommentDao {

    List<Comment> findAllByPostId(Long postId);

    Optional<Comment> findByIdAndPostId(Long id, Long postId);

    Comment save(Comment comment);

    void updateText(Long id, String text);

    void deleteByIdAndPostId(Long id, Long postId);
}
