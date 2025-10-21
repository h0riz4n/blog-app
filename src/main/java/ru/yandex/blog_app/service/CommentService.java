package ru.yandex.blog_app.service;

import java.util.List;
import java.util.Map;

import ru.yandex.blog_app.model.domain.Comment;

public interface CommentService {

    List<Comment> getAllByPostId(Long postId);

    Comment getByIdAndPostId(Long id, Long postId);

    Comment addComment(Long postId, Comment comment);

    Comment updateComment(Long postId, Long commentId, Comment newComment);

    void deleteByIdAndPostId(Long commentId, Long postId);

    Map<Long, Long> commentsCount(List<Long> postIds);
}
