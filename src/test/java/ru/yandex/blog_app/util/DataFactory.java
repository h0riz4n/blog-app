package ru.yandex.blog_app.util;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ru.yandex.blog_app.model.entity.CommentEntity;
import ru.yandex.blog_app.model.entity.PostEntity;
import ru.yandex.blog_app.model.entity.TagEntity;
import ru.yandex.blog_app.repository.CommentRepository;
import ru.yandex.blog_app.repository.PostRepository;
import ru.yandex.blog_app.repository.TagRepository;

public abstract class DataFactory {

    @Autowired
    protected PostRepository postRepo;

    @Autowired
    protected CommentRepository commentRepo;

    @Autowired
    protected TagRepository tagRepo;

    protected CommentEntity firstComment;
    protected CommentEntity secondComment;

    protected PostEntity firstPost;
    protected PostEntity secondPost;

    protected TagEntity firstTag;
    protected TagEntity secondTag;

    protected ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        this.firstPost = createPost("title 1", "text 1");
        this.secondPost = createPost("title 2", "text 2");
    
        this.firstComment = createComment(firstPost, "comment 1");
        this.secondComment = createComment(secondPost, "comment 2");

        this.firstTag = createTag(firstPost, "tag 1");
        this.secondTag = createTag(secondPost, "tag 2");
    }

    @AfterEach
    void clear() {
        postRepo.deleteAll();
    }

    private PostEntity createPost(String title, String text) {
        var post =  PostEntity.builder()
            .title("title 1")
            .text("text 1")
            .build();
        return postRepo.save(post);
    }

    private CommentEntity createComment(PostEntity post, String text) {
        var comment = CommentEntity.builder()
            .post(post)
            .text(text)
            .build();
        return commentRepo.save(comment);
    }

    private TagEntity createTag(PostEntity post, String text) {
        var tag = TagEntity.builder()
            .post(post)
            .text(text)
            .build();
        return tagRepo.save(tag);
    }
}
