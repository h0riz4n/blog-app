package ru.yandex.blog_app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import ru.yandex.blog_app.model.entity.PostEntity;
import ru.yandex.blog_app.model.entity.TagEntity;
import ru.yandex.blog_app.model.filter.PostFilterModel;
import ru.yandex.blog_app.repository.specification.PostSpecification;

@DataJpaTest
public class PostRepositoryTest {

    @Autowired
    private PostRepository postRepo;

    private PostEntity mockPostEntity;

    @BeforeEach
    void setUp() {
        this.mockPostEntity = PostEntity.builder()
            .title("title")
            .text("text")
            .likesCount(0L)
            .build();
        TagEntity mockTag = TagEntity.builder()
            .text("tag1")
            .post(mockPostEntity)
            .build();
        this.mockPostEntity.getTags().add(mockTag);
        this.mockPostEntity = postRepo.save(mockPostEntity);
    }

    @AfterEach
    void finish() {
        postRepo.deleteAll();
    }

    @Test
    public void testFindById() {
        var actualPost = postRepo.findById(mockPostEntity.getId());
        assertEquals(mockPostEntity, actualPost.get());
    }

    @Test
    public void updateById() {
        var actualPost = postRepo.findById(mockPostEntity.getId()).get();
        actualPost.setText("New text");
        assertEquals(actualPost.getText(), postRepo.save(actualPost).getText());
    }

    @Test
    public void getAll() {
        var posts = postRepo.findAll();
        assertEquals(List.of(mockPostEntity), posts);
    }

    @Test
    public void deleteById() {
        postRepo.deleteById(mockPostEntity.getId());
        assertTrue(postRepo.findAll().isEmpty());
    }

    @Test
    public void filterTest() {
        PostFilterModel filter = PostFilterModel.builder()
            .title("title")
            .tags(null)
            .build();

        var posts = postRepo.findAll(new PostSpecification(filter));
        assertEquals(List.of(mockPostEntity), posts);
    }

    @Test
    public void filterTestWithTags() {
        PostFilterModel filter = PostFilterModel.builder()
            .title("title")
            .tags(List.of("tag1"))
            .build();

        var posts = postRepo.findAll(new PostSpecification(filter));
        assertEquals(List.of(mockPostEntity), posts);
    }
}