package ru.yandex.blog_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.yandex.blog_app.model.entity.PostEntity;
import ru.yandex.blog_app.model.entity.TagEntity;
import ru.yandex.blog_app.repository.TagRepository;

@ExtendWith(MockitoExtension.class)
public class TagServiceTest {

    @Mock
    private TagRepository tagRepo;

    @InjectMocks
    private TagService tagService;

    private PostEntity mockPost;
    private TagEntity mockTag;

    @BeforeEach
    void setUp() {
        this.mockPost = PostEntity.builder()
            .id(1L)
            .build();
        this.mockTag = TagEntity.builder()
            .id(1L)
            .post(mockPost)
            .text("tag")
            .build();
    }

    @Test
    public void testGetAllByPostIn() {
        when(tagRepo.findAllByPostIn(List.of(mockPost)))
            .thenReturn(List.of(mockTag));
        
        var expectedTags = List.of(mockTag);
        var actualTags = tagService.getAllByPostIn(List.of(mockPost));
        assertEquals(expectedTags, actualTags);
    }
}
