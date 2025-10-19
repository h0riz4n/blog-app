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
import ru.yandex.blog_app.dao.impl.JdbcPostDao;
import ru.yandex.blog_app.model.domain.Post;
import ru.yandex.blog_app.model.domain.Tag;
import ru.yandex.blog_app.model.dto.Page;

@SpringJUnitConfig(classes = {
    DatabaseConfiguration.class, 
    JdbcPostDao.class
})
@TestPropertySource(locations = "classpath:test-application.properties")
public class PostDaoTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private PostDao postDao;

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
    void save_shouldAddPost() {
        Post mockPost = Post.builder()
            .text("text")
            .title("title")
            .tags(List.of(Tag.builder().text("tag1").build()))
            .build();

        postDao.save(mockPost);
        Page<Post> posts = postDao.findAll("title", List.of("tag1"), 0, 10);
        Post savedPost = postDao.save(mockPost);

        // assertNotNull(savedPost);
        assertEquals("text", savedPost.getText());
        assertEquals("title", savedPost.getTitle());
    }

    @Test
    void findAll() {
        var posts = List.of(
            Post.builder()
                .id(1L)
                .title("Первый пост")
                .text("Текст первого поста")
                .commentsCount(2L)
                .tags(List.of(Tag.builder().id(1L).postId(1L).text("тег1").build()))
                .build(),
            Post.builder()
                .id(2L)
                .title("Второй пост")
                .text("Текст второго поста")
                .commentsCount(2L)
                .tags(List.of(Tag.builder().id(1L).postId(2L).text("тег2").build()))
                .build()
        );

        var expectedPage = Page.<Post>builder()
            .posts(posts)
            .hasNext(false)
            .hasPrev(false)
            .lastPage(0L)
            .build();

        var actualPage = postDao.findAll("", List.of(), 0, 5);

        assertEquals(expectedPage.getPosts(), actualPage.getPosts());
        assertEquals(expectedPage.getHasNext(), actualPage.getHasNext());
        assertEquals(expectedPage.getHasPrev(), actualPage.getHasPrev());
        assertEquals(expectedPage.getLastPage(), expectedPage.getLastPage());
    }

    @Test
    void findById() {
        var expectedPost = Post.builder()
            .id(1L)
            .title("Первый пост")
            .text("Текст первого поста")
            .likesCount(0L)
            .commentsCount(2L)
            .build();

        var actualPost = postDao.findById(1L).get();

        assertNotNull(actualPost);
        assertEquals(expectedPost, actualPost);
    }

    @Test
    void deleteById() {
        assertDoesNotThrow(() -> postDao.deleteById(1L));
    }

    @Test
    void updateText() {
        String newText = "Новый текст";

        postDao.updateText(1L, newText);
        var updatedPost = postDao.findById(1L).get();

        assertEquals(newText, updatedPost.getText());
    }

    @Test
    void updateLikesCount() {
        Long expectedLikes = 1L;
    
        postDao.updateLikesCount(1L, expectedLikes);
        var post = postDao.findById(1L).get();

        assertEquals(expectedLikes, post.getLikesCount());
    }

    @Test
    void updateFileName() {
        String expectedFileName = "test-file-name";

        postDao.updateFileName(1L, expectedFileName);
        var post = postDao.findById(1L).get();
        assertEquals(expectedFileName, post.getFileName());
    }
    
    @Test
    void findFileNameById() {
        String expectedFileName = "test-file-name";
        postDao.updateFileName(1L, expectedFileName);
        String actualFileName = postDao.findFileNameById(1L).get();

        assertEquals(expectedFileName, actualFileName);
    }
}
