package ru.yandex.blog_app.service;

import java.util.List;
import java.util.Map;

import ru.yandex.blog_app.model.domain.Tag;

public interface TagService {

    List<Tag> create(List<Tag> tags);

    Map<Long, List<Tag>> getAllByPostIds(List<Long> postIds);
}
