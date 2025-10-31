package ru.yandex.blog_app.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ru.yandex.blog_app.model.entity.CommentEntity;
import ru.yandex.blog_app.model.entity.PostEntity;
import ru.yandex.blog_app.repository.CommentRepository;
import ru.yandex.blog_app.repository.PostRepository;

public abstract class InitialDataTest {

    @Autowired
    protected PostRepository postRepo;

    @Autowired
    protected CommentRepository commentRepo;

    protected CommentEntity firstComment;
    protected CommentEntity secondComment;

    protected PostEntity firstPost;
    protected PostEntity secondPost;

    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.firstPost = PostEntity.builder()
            .title("title 1")
            .text("text 1")
            .build();
        firstPost.setId(postRepo.save(firstPost).getId());

        this.secondPost = PostEntity.builder()
            .title("title 2")
            .text("text 2")
            .build();
        secondPost.setId(postRepo.save(secondPost).getId());
        
        this.firstComment = CommentEntity.builder()
            .post(firstPost)
            .text("comment 1")
            .build();
        firstComment.setId(commentRepo.save(firstComment).getId());

        this.secondComment = CommentEntity.builder()
            .post(secondPost)
            .text("comment 2")
            .build();
        secondComment.setId(commentRepo.save(secondComment).getId());
    }

    @AfterEach
    void clear() {
        postRepo.deleteAll();
    }
}
