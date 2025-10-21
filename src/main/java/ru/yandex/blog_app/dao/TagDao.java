package ru.yandex.blog_app.dao;

import java.util.List;
import java.util.Map;

import ru.yandex.blog_app.model.domain.Tag;

public interface TagDao {

    List<Tag> saveAll(List<Tag> tags);

    Map<Long, List<Tag>> findAllByPostIds(List<Long> postIds);
}
