package ru.yandex.blog_app.service;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.exception.ApiServiceException;
import ru.yandex.blog_app.model.entity.CommentEntity;
import ru.yandex.blog_app.model.entity.PostEntity;
import ru.yandex.blog_app.repository.CommentRepository;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepo;

    public List<CommentEntity> getAllByPostIn(List<PostEntity> posts) {
        return commentRepo.findAllByPostIn(posts);
    }

    public List<CommentEntity> getAllByPostId(Long postId) {
        return commentRepo.findAllByPostId(postId);
    }

    public CommentEntity getByIdAndPostId(Long id, Long postId) {
        return commentRepo.findByIdAndPostId(id, postId)
            .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND, "Кооментарий не найден"));
    }

    @Transactional
    public CommentEntity create(PostEntity post, CommentEntity comment) {
        comment.setPost(post);
        validateOnCreate(post.getId(), comment);
        return commentRepo.save(comment);
    }

    @Transactional
    public CommentEntity update(PostEntity post, Long id, CommentEntity newComment) {
        CommentEntity comment = commentRepo.findById(id)
            .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND, "Комментарий не найден"));

        validateOnUpdate(post.getId(), id, newComment);

        comment.setText(newComment.getText());
        return commentRepo.save(comment);
    }

    public void deleteById(Long id) {
        commentRepo.deleteById(id);
    }

    private void validateOnCreate(Long postId, CommentEntity comment) {
        if (!comment.getPost().getId().equals(postId)) {
            throw new ApiServiceException(HttpStatus.BAD_REQUEST, "Не совпадают идентификаторы поста в теле и в пути");
        }
    }

    private void validateOnUpdate(Long postId, Long commentId, CommentEntity comment) {
        validateOnCreate(postId, comment);

        if (!comment.getId().equals(commentId)) {
            throw new ApiServiceException(HttpStatus.BAD_REQUEST, "Не совпадают идентификаторы комментария в теле и в пути");
        }
    }
}
