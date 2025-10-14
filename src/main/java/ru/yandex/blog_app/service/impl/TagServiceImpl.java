package ru.yandex.blog_app.service.impl;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.dao.TagDao;
import ru.yandex.blog_app.model.domain.Tag;
import ru.yandex.blog_app.service.TagService;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagDao tagDao;

    @Override
    public List<Tag> create(List<Tag> tags) {
        return tagDao.saveAll(tags);
    }

    @Override
    public Map<Long, List<Tag>> getAllByPostIds(List<Long> postIds) {
        return tagDao.findAllByPostIds(postIds);
    }
}
