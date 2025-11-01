package ru.yandex.blog_app.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;

import ru.yandex.blog_app.model.entity.CommentEntity;
import ru.yandex.blog_app.model.entity.PostEntity;

@Repository
public interface CommentRepository extends JpaRepository<CommentEntity, Long> {

    List<CommentEntity> findAllByPostIn(List<PostEntity> posts);

    List<CommentEntity> findAllByPostId(Long postId);

    Optional<CommentEntity> findByIdAndPostId(Long id, Long postId);

    @Modifying
    void deleteByIdAndPostId(Long id, Long postId);
}
