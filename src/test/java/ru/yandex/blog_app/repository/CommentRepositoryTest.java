package ru.yandex.blog_app.repository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import ru.yandex.blog_app.model.entity.CommentEntity;
import ru.yandex.blog_app.model.entity.PostEntity;

@DataJpaTest
public class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepo;

    @Autowired
    private PostRepository postRepo;

    private PostEntity mockPost;
    private CommentEntity mockComment;

    @BeforeEach
    void setUp() {
        this.mockPost = PostEntity.builder()
            .title("title")
            .text("text")
            .likesCount(0L)
            .build();
        this.mockComment = CommentEntity.builder()
            .text("comment")
            .post(mockPost)
            .build();
        this.mockPost.getComments().add(mockComment);
        this.mockPost.setId(postRepo.save(mockPost).getId());
        this.mockComment.setId(commentRepo.save(mockComment).getId());;
    }

    @AfterEach
    void afterEach() {
        commentRepo.deleteAll();
        postRepo.deleteAll();
    }

    @Test
    public void testFindAllByPostIn() {
        var comments = commentRepo.findAllByPostIn(List.of(mockPost));
        assertEquals(List.of(mockComment), comments);
    }

    @Test
    public void testFindAllByPostId() {
        var comments = commentRepo.findAllByPostId(mockPost.getId());
        assertEquals(List.of(mockComment), comments);
    }

    @Test
    public void testFindByIdAndPostId() {
        var comment = commentRepo.findByIdAndPostId(mockComment.getId(), mockPost.getId());
        assertTrue(comment.isPresent());
        assertEquals(mockComment, comment.get());
    }
}
