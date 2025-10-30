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

import ru.yandex.blog_app.model.dto.PostDto;
import ru.yandex.blog_app.model.view.PostView;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PostControllerTest {

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
    public void create() throws Exception {
        PostDto mockDto = PostDto.builder()
            .title("title")
            .text("text")
            .tags(List.of("tag1", "tag2"))
            .build();

        var mockRequestBody = post("/api/posts")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writerWithView(PostView.Create.class).writeValueAsString(mockDto));
        
        var mockBadRequestBody = post("/api/posts")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writerWithView(PostView.Create.class).writeValueAsString(mockDto.toBuilder().text("").build()));

        mockMvc.perform(mockRequestBody)
            .andExpect(status().isCreated())
            .andDo(print())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(mockBadRequestBody)
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void getAll() throws Exception {
        var mockRequest = get("/api/posts")
            .param("search", "пост #тег1")
            .param("pageNumber", "0")
            .param("pageSize", "2");
        
        mockMvc.perform(mockRequest)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void getById() throws Exception {
        mockMvc.perform(get("/api/posts/{id}", 2L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/posts/{id}", 999L))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void updateById() throws Exception {
        PostDto mockDto = PostDto.builder()
            .id(1L)
            .title("title")
            .text("new text")
            .tags(List.of("tag1", "tag2"))
            .build();

        var mockRequest = put("/api/posts/{id}", 1L)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writerWithView(PostView.Modify.class).writeValueAsString(mockDto));

        var mockNotFoundRequest = put("/api/posts/{id}", 999L)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writerWithView(PostView.Modify.class).writeValueAsString(mockDto));

        var mockBadRequestRequst = put("/api/posts/{id}", 1L)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writerWithView(PostView.Modify.class).writeValueAsString(mockDto.toBuilder().text("").build()));

        mockMvc.perform(mockRequest)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        
        mockMvc.perform(mockNotFoundRequest)
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        mockMvc.perform(mockBadRequestRequst)
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void deleteById() throws Exception {
        mockMvc.perform(delete("/api/posts/{id}", 1L))
            .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/posts/{id}", 0L))
            .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/posts/{id}", -1L))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void like() throws Exception {
        mockMvc.perform(post("/api/posts/{id}/likes", 1L))
            .andExpect(status().isOk())
            .andExpect(content().string("1"));

        mockMvc.perform(post("/api/posts/{id}/likes", 999L))
            .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/posts/{id}/likes", 0L))
            .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/posts/{id}/likes", -1L))
            .andExpect(status().isBadRequest());
    }
}
