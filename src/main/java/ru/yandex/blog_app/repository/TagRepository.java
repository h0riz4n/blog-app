package ru.yandex.blog_app.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import ru.yandex.blog_app.model.entity.PostEntity;
import ru.yandex.blog_app.model.entity.TagEntity;

@Repository
public interface TagRepository extends JpaRepository<TagEntity, Long> {

    List<TagEntity> findAllByPostIn(List<PostEntity> posts);
}
