package ru.yandex.blog_app.controller;

import java.util.List;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ru.yandex.blog_app.model.dto.PostDto;
import ru.yandex.blog_app.model.view.PostView;
import ru.yandex.blog_app.util.DataFactory;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class PostControllerTest extends DataFactory {

    @Autowired
    private MockMvc mockMvc;

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
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasKey("id")));

        mockMvc.perform(mockBadRequestBody)
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void getAll() throws Exception {
        var mockRequest = get("/api/posts")
            .param("search", "title")
            .param("pageNumber", "0")
            .param("pageSize", "10");
        
        mockMvc.perform(mockRequest)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.posts", hasSize(2)));
    }

    @Test
    public void getById() throws Exception {
        mockMvc.perform(get("/api/posts/{id}", firstPost.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.id").value(firstPost.getId()));

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

        var mockRequest = put("/api/posts/{id}", firstPost.getId())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writerWithView(PostView.Modify.class).writeValueAsString(mockDto));

        var mockNotFoundRequest = put("/api/posts/{id}", 999L)
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writerWithView(PostView.Modify.class).writeValueAsString(mockDto));

        var mockBadRequestRequst = put("/api/posts/{id}", firstPost.getId())
            .contentType(MediaType.APPLICATION_JSON_VALUE)
            .content(objectMapper.writerWithView(PostView.Modify.class).writeValueAsString(mockDto.toBuilder().text("").build()));

        mockMvc.perform(mockRequest)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.text").value(mockDto.getText()));
        
        mockMvc.perform(mockNotFoundRequest)
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));

        mockMvc.perform(mockBadRequestRequst)
            .andExpect(status().isBadRequest())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void deleteById() throws Exception {
        mockMvc.perform(delete("/api/posts/{id}", firstPost.getId()))
            .andExpect(status().isNoContent());

        mockMvc.perform(delete("/api/posts/{id}", 0L))
            .andExpect(status().isBadRequest());

        mockMvc.perform(delete("/api/posts/{id}", -1L))
            .andExpect(status().isBadRequest());
    }

    @Test
    public void like() throws Exception {
        mockMvc.perform(post("/api/posts/{id}/likes", firstPost.getId()))
            .andExpect(status().isOk())
            .andExpect(content().string("%s".formatted(firstPost.getLikesCount() + 1)));

        mockMvc.perform(post("/api/posts/{id}/likes", 999L))
            .andExpect(status().isNotFound());

        mockMvc.perform(post("/api/posts/{id}/likes", 0L))
            .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/posts/{id}/likes", -1L))
            .andExpect(status().isBadRequest());
    }
}
