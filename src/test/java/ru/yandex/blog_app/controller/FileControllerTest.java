package ru.yandex.blog_app.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import ru.yandex.blog_app.WebConfiguration;
import ru.yandex.blog_app.configuration.DatabaseConfiguration;

@SpringJUnitConfig(classes = {
    WebConfiguration.class,
    DatabaseConfiguration.class

})
@WebAppConfiguration
@TestPropertySource(locations = "classpath:test-application.properties")
public class FileControllerTest {

    @Autowired
    private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;
    private JdbcTemplate jdbcTemplate;
    private byte[] mockPngStup;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() throws Exception {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        this.jdbcTemplate = namedParameterJdbcTemplate.getJdbcTemplate();
        this.mockPngStup = new byte[]{(byte) 137, 80, 78, 71};
        this.mockFile = new MockMultipartFile("image", "avatar.png", "image/png", mockPngStup);

        jdbcTemplate.execute("""
            INSERT INTO blog_app.post (title, text, likes_count) VALUES 
            ('Первый пост', 'Текст первого поста', 0),
            ('Второй пост', 'Текст второго поста', 0);
        """);
        
        jdbcTemplate.execute("""
            INSERT INTO blog_app.tag (post_id, text) VALUES (1, 'тег1'), (2, 'тег2');
        """);
        
        jdbcTemplate.execute("""
            INSERT INTO blog_app.comment (post_id, text) VALUES
            (1, 'Комментарий 1 к первому посту'),
            (1, 'Комментарий 2 к первому посту'),
            (2, 'Комментарий 1 ко второму посту'),
            (2, 'Комментарий 2 ко второму посту');
        """);
    }

    @AfterEach
    void clear() throws IOException {
        jdbcTemplate.execute("DELETE FROM blog_app.post");
        jdbcTemplate.execute("DELETE FROM blog_app.tag");
        jdbcTemplate.execute("DELETE FROM blog_app.comment");

        jdbcTemplate.execute("ALTER TABLE blog_app.post ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE blog_app.tag ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE blog_app.comment ALTER COLUMN id RESTART WITH 1");

        Path folder = Paths.get("./uploads");
        if (Files.exists(folder)) {
            Files.walk(folder)
                .sorted(Comparator.reverseOrder())
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
        }
    }

    @Test
    public void upload() throws Exception {
        var mockRequest = multipart(HttpMethod.PUT, "/api/posts/{id}/image", 1L)
            .file(mockFile)
            .contentType(MediaType.MULTIPART_FORM_DATA);
        
        var mockNotFoundReq = multipart(HttpMethod.PUT, "/api/posts/{id}/image", 999L)
            .file(mockFile)
            .contentType(MediaType.MULTIPART_FORM_DATA);

        var mockBadRequestReq = multipart(HttpMethod.PUT, "/api/posts/{id}/image", 1L)
            .contentType(MediaType.MULTIPART_FORM_DATA);

        mockMvc.perform(mockRequest)
            .andDo(print())
            .andExpect(status().isOk());

        mockMvc.perform(mockNotFoundReq)
            .andExpect(status().isNotFound());

        mockMvc.perform(mockBadRequestReq)
            .andExpect(status().isBadRequest());
    }

    @Test
    public void download() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}/image", 1L))
            .andDo(print())
            .andExpect(status().isNotFound());

        var mockRequest = multipart(HttpMethod.PUT, "/api/posts/{id}/image", 1L)
            .file(mockFile)
            .contentType(MediaType.MULTIPART_FORM_DATA);
        
        mockMvc.perform(mockRequest)
            .andDo(print())
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/{postId}/image", 1L))
            .andExpect(status().isOk())
            .andDo(print());
    }
}
