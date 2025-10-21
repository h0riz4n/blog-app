package ru.yandex.blog_app.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ru.yandex.blog_app.WebConfiguration;
import ru.yandex.blog_app.configuration.DatabaseConfiguration;
import ru.yandex.blog_app.model.dto.CommentDto;
import ru.yandex.blog_app.model.view.CommentView;

@SpringJUnitConfig(classes = {
    WebConfiguration.class,
    DatabaseConfiguration.class
})
@WebAppConfiguration
@TestPropertySource(locations = "classpath:test-application.properties")
public class CommentControllerTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private ObjectMapper objectMapper;
    private MockMvc mockMvc;
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() throws Exception {
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
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
    public void getAllByPostId() throws Exception {
        var comments = List.of(
            CommentDto.builder().id(1L).postId(1L).text("Комментарий 1 к первому посту").build(),
            CommentDto.builder().id(2L).postId(1L).text("Комментарий 2 к первому посту").build()
        );

        mockMvc.perform(get("/api/posts/{postId}/comments", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(objectMapper.writeValueAsString(comments)));
    }

    @Test
    public void getByIdAndPostId() throws Exception {
        var comment = CommentDto.builder().id(1L).postId(1L).text("Комментарий 1 к первому посту").build();

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", 1L, 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(objectMapper.writeValueAsString(comment)));

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", 999L, 999L))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void addComment() throws Exception {
        var comment = CommentDto.builder().postId(1L).text("Комментарий 3 к первому посту").build();

        var mocKRequest = post("/api/posts/{postId}/comments", 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writerWithView(CommentView.Create.class).writeValueAsString(comment));

        mockMvc.perform(mocKRequest)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(objectMapper.writerWithView(CommentView.Details.class).writeValueAsString(comment.toBuilder().id(5L).build())));
    }

    @Test
    public void update() throws Exception {
        var comment = CommentDto.builder().id(1L).postId(1L).text("Обновлённый комментарий 1 к первому посту").build();

        var mocKRequest = put("/api/posts/{postId}/comments/{commentId}", 1L, 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writerWithView(CommentView.Update.class).writeValueAsString(comment));

        mockMvc.perform(mocKRequest)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(objectMapper.writeValueAsString(comment)));
    }

    @Test
    public void deleteByPostIdAndId() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 1L, 1L))
            .andExpect(status().isOk());
    }
}
