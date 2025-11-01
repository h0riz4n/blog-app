package ru.yandex.blog_app.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.yandex.blog_app.exception.ApiServiceException;
import ru.yandex.blog_app.model.entity.CommentEntity;
import ru.yandex.blog_app.model.entity.PostEntity;
import ru.yandex.blog_app.repository.CommentRepository;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentRepository commentRepo;

    @InjectMocks
    private CommentService commentService;

    private PostEntity mockPost;
    private CommentEntity mockComment;

    @BeforeEach
    void setUp() {
        this.mockPost = PostEntity.builder()
            .id(1L)
            .build();
        this.mockComment = CommentEntity.builder()
            .id(1L)
            .post(mockPost)
            .text("comment")
            .build();
    }

    @Test
    public void testGetAllByPostIn() {
        var expectedComments = List.of(mockComment);

        when(commentRepo.findAllByPostIn(List.of(mockPost)))
            .thenReturn(expectedComments);

        var actualComments = commentService.getAllByPostIn(List.of(mockPost));

        assertEquals(expectedComments, actualComments);
    }

    @Test
    public void testGetAllByPostId() {
        var expectedComments = List.of(mockComment);

        when(commentRepo.findAllByPostId(mockPost.getId()))
            .thenReturn(expectedComments);

        var actualComments = commentService.getAllByPostId(mockPost.getId());

        assertEquals(expectedComments, actualComments);
    }

    @Test
    public void testGetByIdAndPostId() {
        when(commentRepo.findByIdAndPostId(mockComment.getId(), mockPost.getId()))
            .thenReturn(Optional.of(mockComment));

        var actualComment = commentService.getByIdAndPostId(mockComment.getId(), mockPost.getId());
        assertEquals(mockComment, actualComment);
    }

    @Test
    public void testGetByIdAndPostIdThrowsApiServiceException() {
        when(commentRepo.findByIdAndPostId(mockComment.getId(), mockPost.getId()))
            .thenReturn(Optional.empty());


        assertThrows(ApiServiceException.class, () -> {
            commentService.getByIdAndPostId(mockComment.getId(), mockPost.getId());
        });
    }

    @Test
    public void create() {
        var newComment = CommentEntity.builder()
            .text("comment")
            .build();

        when(commentRepo.save(newComment))
            .thenReturn(newComment.toBuilder().post(mockPost).id(2L).build());

        var actualComment = commentService.create(mockPost, newComment);

        assertEquals(mockPost, actualComment.getPost());
        assertEquals(2L, actualComment.getId());
    }

    @Test
    public void testUpdate() {
        var newComment = mockComment.toBuilder()
            .text("new comment")
            .build();

        var expectedComment = mockComment.toBuilder()
            .text("new comment")
            .build();

        when(commentRepo.findById(1L))
            .thenReturn(Optional.of(mockComment));

        when(commentRepo.save(expectedComment))
            .thenReturn(expectedComment);

        var actualComment = commentService.update(mockPost, 1L, newComment);

        assertEquals(expectedComment, actualComment);
    }

    @Test
    public void testDeleteById() {
        doNothing().when(commentRepo).deleteById(anyLong());

        assertDoesNotThrow(() -> {
            commentService.deleteById(1L);
        });
    }
}
