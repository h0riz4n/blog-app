package ru.yandex.blog_app.controller;

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
import ru.yandex.blog_app.model.dto.PostDto;
import ru.yandex.blog_app.model.view.PostView;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitConfig(classes = {
    WebConfiguration.class,
    DatabaseConfiguration.class

})
@WebAppConfiguration
@TestPropertySource(locations = "classpath:test-application.properties")
public class PostControllerTest {
    
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
    public void create() throws Exception {
        PostDto mockDto = PostDto.builder()
            .title("title")
            .text("text")
            .tags(List.of("tag1", "tag2"))
            .build();

        var mockRequestBody = post("/api/posts")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writerWithView(PostView.Summary.class).writeValueAsString(mockDto));
        
        var mockBadRequestBody = post("/api/posts")
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writerWithView(PostView.Summary.class).writeValueAsString(mockDto.toBuilder().text("").build()));

        mockMvc.perform(mockRequestBody)
            .andExpect(status().isCreated())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(mockBadRequestBody)
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void getAll() throws Exception {
        var mockRequest = get("/api/posts")
            .param("search", "пост")
            .param("pageNumber", "0")
            .param("pageSize", "2");
        
        mockMvc.perform(mockRequest)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void getById() throws Exception {        
        mockMvc.perform(get("/api/posts/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(get("/api/posts/{id}", 999L))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
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
            .content(objectMapper.writerWithView(PostView.Update.class).writeValueAsString(mockDto));

        var mockNotFoundRequest = put("/api/posts/{id}", 999L)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writerWithView(PostView.Update.class).writeValueAsString(mockDto));

        var mockBadRequestRequst = put("/api/posts/{id}", 1L)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writerWithView(PostView.Update.class).writeValueAsString(mockDto.toBuilder().text("").build()));

        mockMvc.perform(mockRequest)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
        
        mockMvc.perform(mockNotFoundRequest)
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));

        mockMvc.perform(mockBadRequestRequst)
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void deleteById() throws Exception {
        mockMvc.perform(delete("/api/posts/{id}", 1L))
            .andExpect(status().isOk());

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
