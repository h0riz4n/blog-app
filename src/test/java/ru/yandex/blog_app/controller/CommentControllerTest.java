package ru.yandex.blog_app.controller;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import ru.yandex.blog_app.model.dto.CommentDto;
import ru.yandex.blog_app.model.view.CommentView;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@Tag("integration")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class CommentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() throws Exception {
        this.objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        jdbcTemplate.execute("""
            INSERT INTO blog_app.post (id, title, text, likes_count) VALUES 
            (1, 'Первый пост', 'Текст первого поста', 0),
            (2, 'Второй пост', 'Текст второго поста', 0);
        """);
        
        jdbcTemplate.execute("""
            INSERT INTO blog_app.tag (id, post_id, text) VALUES (1, 1, 'тег1'), (2, 2, 'тег2');
        """);
        
        jdbcTemplate.execute("""
            INSERT INTO blog_app.comment (id, post_id, text) VALUES
            (1, 1, 'Комментарий 1 к первому посту'),
            (2, 1, 'Комментарий 2 к первому посту'),
            (3, 2, 'Комментарий 1 ко второму посту'),
            (4, 2, 'Комментарий 2 ко второму посту');
        """);

        jdbcTemplate.execute("ALTER SEQUENCE blog_app.post_id_seq RESTART WITH 3");
        jdbcTemplate.execute("ALTER SEQUENCE blog_app.tag_id_seq RESTART WITH 3");
        jdbcTemplate.execute("ALTER SEQUENCE blog_app.comment_id_seq RESTART WITH 5");
    }

    @AfterEach
    void clear() {
        jdbcTemplate.execute("DELETE FROM blog_app.comment");
        jdbcTemplate.execute("DELETE FROM blog_app.tag");
        jdbcTemplate.execute("DELETE FROM blog_app.post");
        jdbcTemplate.execute("ALTER SEQUENCE blog_app.post_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE blog_app.tag_id_seq RESTART WITH 1");
        jdbcTemplate.execute("ALTER SEQUENCE blog_app.comment_id_seq RESTART WITH 1");
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
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
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
            .andExpect(content().string(objectMapper.writeValueAsString(comment.toBuilder().id(5L).build())));
    }

    @Test
    public void update() throws Exception {
        var comment = CommentDto.builder().id(1L).postId(1L).text("Обновлённый комментарий 1 к первому посту").build();

        var mocKRequest = put("/api/posts/{postId}/comments/{commentId}", 1L, 1L)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writerWithView(CommentView.Modify.class).writeValueAsString(comment));

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
