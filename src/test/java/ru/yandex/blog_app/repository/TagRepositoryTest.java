package ru.yandex.blog_app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import ru.yandex.blog_app.model.entity.PostEntity;
import ru.yandex.blog_app.model.entity.TagEntity;

@DataJpaTest
public class TagRepositoryTest {

    @Autowired
    private PostRepository postRepo;

    @Autowired
    private TagRepository tagRepo;

    private PostEntity mockPost;
    private TagEntity mockTag;

    @BeforeEach
    void beforeEach() {
        this.mockPost = PostEntity.builder()
            .title("title")
            .text("text")
            .likesCount(0L)
            .build();
        mockPost.setId(postRepo.save(mockPost).getId());

        this.mockTag = TagEntity.builder()
            .text("tag1")
            .post(mockPost)
            .build();
        mockTag.setId(tagRepo.save(mockTag).getId());
    }

    @AfterEach
    void afterEach() {
        postRepo.deleteAll();
    }

    @Test
    public void testSave() {
        var newTag = TagEntity.builder()
            .text("text 1")
            .post(mockPost)
            .build();

        assertEquals(mockPost.getId(), tagRepo.save(newTag).getPost().getId());
    }

    @Test
    public void getAllByPostIn() {
        var tags = tagRepo.findAllByPostIn(List.of(mockPost));
        assertFalse(tags.isEmpty());
        assertEquals(List.of(mockTag), tags);
    }
}
