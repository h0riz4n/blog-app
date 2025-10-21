package ru.yandex.blog_app.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import ru.yandex.blog_app.configuration.DatabaseConfiguration;
import ru.yandex.blog_app.dao.impl.JdbcCommentDao;
import ru.yandex.blog_app.model.domain.Comment;

@SpringJUnitConfig(classes = {
    DatabaseConfiguration.class, 
    JdbcCommentDao.class
})
@TestPropertySource(locations = "classpath:test-application.properties")
public class CommentDaoTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private CommentDao commentDao;

    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        this.jdbcTemplate = namedParameterJdbcTemplate.getJdbcTemplate();

        jdbcTemplate.execute("""
            INSERT INTO blog_app.post (title, text, likes_count) VALUES 
            ('Первый пост', 'Текст первого поста', 0),
            ('Второй пост', 'Текст второго поста', 0);
        """);
        
        jdbcTemplate.execute("""
            INSERT INTO blog_app.tag (post_id, text) VALUES (1L, 'тег1'), (2L, 'тег2');
        """);
        
        jdbcTemplate.execute("""
            INSERT INTO blog_app.comment (post_id, text) VALUES
            (1L, 'Комментарий 1 к первому посту'),
            (1L, 'Комментарий 2 к первому посту'),
            (2L, 'Комментарий 1 ко второму посту'),
            (2L, 'Комментарий 2 ко второму посту');
        """);
    }

    @AfterEach
    void clear() {
        jdbcTemplate.execute("DELETE FROM blog_app.post");
        jdbcTemplate.execute("DELETE FROM blog_app.tag");
        jdbcTemplate.execute("DELETE FROM blog_app.comment");

        jdbcTemplate.execute("ALTER TABLE blog_app.post ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE blog_app.tag ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE blog_app.comment ALTER COLUMN id RESTART WITH 1");
    }

    @Test
    void findAllByPostId() {
        var expectedComments = List.of(
            Comment.builder().id(1L).postId(1L).text("Комментарий 1 к первому посту").build(),
            Comment.builder().id(2L).postId(1L).text("Комментарий 2 к первому посту").build()
        );
        
        var actualComments = commentDao.findAllByPostId(1L);

        assertFalse(actualComments.isEmpty());
        assertEquals(expectedComments, actualComments);
    }

    @Test
    void findByIdAndPostId() {
        var expectedComment = Comment.builder()
            .id(1L)
            .postId(1L)
            .text("Комментарий 1 к первому посту")
            .build();

        var actualComment = commentDao.findByIdAndPostId(1L, 1L).get();
        assertEquals(expectedComment, actualComment);
        assertTrue(commentDao.findByIdAndPostId(5L, 1L).isEmpty());
    }

    @Test
    void save() {
        var newComment = Comment.builder()
            .postId(1L)
            .text("Комментарий 3 к первому посту")
            .build();

        var savedComment = commentDao.save(newComment);
        
        assertEquals(5L, savedComment.getId().longValue());
    }

    @Test
    void updateText() {
        String newText = "New text";
        var comment = commentDao.findByIdAndPostId(1L, 1L).get();

        commentDao.updateText(comment.getId(), newText);

        var updatedComment = commentDao.findByIdAndPostId(comment.getId(), comment.getPostId()).get();

        assertNotEquals(comment.getText(), updatedComment.getText());
        assertEquals(newText, updatedComment.getText());
    }

    @Test
    void deleteByIdAndPostId() {
        assertDoesNotThrow(() -> commentDao.deleteByIdAndPostId(1L, 1L));
    }
}
