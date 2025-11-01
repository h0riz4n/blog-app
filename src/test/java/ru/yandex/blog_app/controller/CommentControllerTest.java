package ru.yandex.blog_app.controller;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import ru.yandex.blog_app.model.dto.CommentDto;
import ru.yandex.blog_app.model.view.CommentView;
import ru.yandex.blog_app.util.DataFactory;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Tag("integration")
@AutoConfigureMockMvc
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
public class CommentControllerTest extends DataFactory {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void getAllByPostId() throws Exception {
        mockMvc.perform(get("/api/posts/{postId}/comments", firstPost.getId()))
            .andDo(print())
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(firstComment.getId()));
    }

    @Test
    public void getByIdAndPostId() throws Exception {
        var comment = CommentDto.builder()
            .id(firstComment.getId())
            .postId(firstPost.getId())
            .text(firstComment.getText())
            .build();

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", firstPost.getId(), firstComment.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(content().string(objectMapper.writeValueAsString(comment)));

        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", 999L, 999L))
            .andExpect(status().isNotFound())
            .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    public void addComment() throws Exception {
        var comment = CommentDto.builder()
            .postId(firstPost.getId())
            .text("new comment")
            .build();

        var mocKRequest = post("/api/posts/{postId}/comments", firstPost.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writerWithView(CommentView.Create.class).writeValueAsString(comment));

        mockMvc.perform(mocKRequest)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasKey("id")));
    }

    @Test
    public void update() throws Exception {
        var comment = CommentDto.builder()
            .id(firstComment.getId())
            .postId(firstComment.getPost().getId())
            .text("Обновлённый комментарий 1 к первому посту")
            .build();

        var mocKRequest = put("/api/posts/{postId}/comments/{commentId}", firstPost.getId(), firstComment.getId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writerWithView(CommentView.Modify.class).writeValueAsString(comment));

        mockMvc.perform(mocKRequest)
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasEntry("text", comment.getText())));
    }

    @Test
    public void deleteByPostIdAndId() throws Exception {
        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", firstPost.getId(), firstComment.getId()))
            .andExpect(status().isOk());

        mockMvc.perform(get("/api/posts/{postId}/comments", firstPost.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }
}
