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
        this.mockPostEntity.setId(postRepo.save(mockPostEntity).getId());
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
}