package ru.yandex.blog_app.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.dao.CommentDao;
import ru.yandex.blog_app.exception.ApiServiceException;
import ru.yandex.blog_app.model.domain.Comment;
import ru.yandex.blog_app.service.CommentService;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentDao commentDao;

    @Override
    public List<Comment> getAllByPostId(Long postId) {
        return commentDao.findAllByPostId(postId);
    }

    @Override
    public Comment getByIdAndPostId(Long id, Long postId) {
        return commentDao.findByIdAndPostId(id, postId)
            .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND, "Комментарий не найден"));
    }

    @Override
    @Transactional
    public Comment addComment(Long postId, Comment comment) {
        validateOnCreate(postId, comment);
        return commentDao.save(comment);
    }

    @Override
    @Transactional
    public Comment updateComment(Long postId, Long commentId, Comment newComment) {
        validateOnUpdate(postId, commentId, newComment);

        Comment comment = getByIdAndPostId(newComment.getId(), newComment.getPostId());
        comment.setText(newComment.getText());
        commentDao.updateText(comment.getId(), comment.getText());
        return comment;
    }

    @Override
    public void deleteByIdAndPostId(Long commentId, Long postId) {
        commentDao.deleteByIdAndPostId(commentId, postId);
    }

    @Override
    public Map<Long, Long> commentsCount(List<Long> postIds) {
        return commentDao.countAllByPostIds(postIds);
    }

    private void validateOnCreate(Long postId, Comment comment) {
        if (!comment.getPostId().equals(postId)) {
            throw new ApiServiceException(HttpStatus.BAD_REQUEST, "Не совпадают идентификаторы поста в теле и в пути");
        }
    }

    private void validateOnUpdate(Long postId, Long commentId, Comment comment) {
        if (!comment.getPostId().equals(postId)) {
            throw new ApiServiceException(HttpStatus.BAD_REQUEST, "Не совпадают идентификаторы поста в теле и в пути");
        }

        if (!comment.getId().equals(commentId)) {
            throw new ApiServiceException(HttpStatus.BAD_REQUEST, "Не совпадают идентификаторы комментария в теле и в пути");
        }
    }
}
