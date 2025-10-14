package ru.yandex.blog_app.dao;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
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
import ru.yandex.blog_app.dao.impl.JdbcTagDao;
import ru.yandex.blog_app.model.domain.Tag;

@SpringJUnitConfig(classes = {
    DatabaseConfiguration.class, 
    JdbcTagDao.class
})
@TestPropertySource(locations = "classpath:test-application.properties")
public class TagDaoTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private TagDao tagDao;

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
    void saveAll() {
        var tags = List.of(
            Tag.builder().postId(1L).text("tag2").build(),
            Tag.builder().postId(1L).text("tag3").build()
        );
        
        var savedTags = tagDao.saveAll(tags);
        savedTags.forEach(tag -> {
            assertNotNull(tag.getId());
        });
    }

    @Test
    void findAllByPostIds() {
        var tags = tagDao.findAllByPostIds(List.of(1L));
        
        assertFalse(tags.getOrDefault(1L, Collections.emptyList()).isEmpty());

        var tag = tags.getOrDefault(1L, Collections.emptyList()).getFirst();

        assertEquals(1L, tag.getId().longValue());
    }
}
