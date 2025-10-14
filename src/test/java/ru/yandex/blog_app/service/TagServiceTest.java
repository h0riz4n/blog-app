package ru.yandex.blog_app.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.yandex.blog_app.dao.TagDao;
import ru.yandex.blog_app.model.domain.Tag;
import ru.yandex.blog_app.service.impl.TagServiceImpl;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {

    @Mock
    private TagDao tagDao;

    @InjectMocks
    private TagServiceImpl tagService;

    private Long mockPostId;
    private Long mockTagId;
    private Tag mockTag;

    @BeforeEach
    public void setUp() {
        this.mockPostId = 1L;
        this.mockTagId = 1L;
        this.mockTag = Tag.builder()
            .postId(mockPostId)
            .id(mockTagId)
            .text("text")
            .build();
    }

    @Test
    public void create() {
        when(tagDao.saveAll(List.of(mockTag)))
            .thenReturn(List.of(mockTag));

        assertEquals(List.of(mockTag), tagService.create(List.of(mockTag)));
    }

    @Test
    public void getAllByPostIds() {
        var mockPostIds = List.of(mockPostId);
        var mockMapOfTags = Map.of(
            mockPostId, List.of(mockTag)
        );

        when(tagDao.findAllByPostIds(mockPostIds))
            .thenReturn(mockMapOfTags);

        var mapOfTags = tagService.getAllByPostIds(mockPostIds);
        assertEquals(mockMapOfTags, mapOfTags);
        assertNotNull(mapOfTags.get(mockPostId));
    }
}
