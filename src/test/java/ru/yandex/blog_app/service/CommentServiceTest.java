package ru.yandex.blog_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import ru.yandex.blog_app.dao.CommentDao;
import ru.yandex.blog_app.exception.ApiServiceException;
import ru.yandex.blog_app.model.domain.Comment;
import ru.yandex.blog_app.service.impl.CommentServiceImpl;

@ExtendWith(MockitoExtension.class)
public class CommentServiceTest {

    @Mock
    private CommentDao commentDao;

    @InjectMocks
    private CommentServiceImpl commentService;

    private Long mockPostId;
    private Long mockCommentId;
    private Comment mockComment;

    @BeforeEach
    public void setUp() {
        this.mockPostId = 1L;
        this.mockCommentId = 1L;
        this.mockComment = Comment.builder()
            .id(mockCommentId)
            .text("text")
            .postId(mockPostId)
            .build();
    }

    @Test
    public void getAllByPostId() {
        when(commentDao.findAllByPostId(mockPostId))
            .thenReturn(List.of(mockComment));

        List<Comment> comments = commentService.getAllByPostId(mockPostId);
        assertFalse(comments.isEmpty());
    }

    @Test
    public void getByIdAndPostId() {
        when(commentDao.findByIdAndPostId(mockCommentId, mockPostId))
            .thenReturn(Optional.of(mockComment));

        assertEquals(mockComment, commentService.getByIdAndPostId(mockCommentId, mockPostId));
        assertThrows(ApiServiceException.class, () -> commentService.getByIdAndPostId(2L, 2L));
    }

    @Test
    public void addComment() {
        when(commentDao.save(mockComment))
            .thenReturn(mockComment);
        
        Comment comment = commentService.addComment(mockPostId, mockComment);
        assertEquals(mockCommentId, comment.getId());
        assertThrows(ApiServiceException.class, () -> commentService.addComment(2L, mockComment));
    }

    @Test
    public void updateComment() {
        when(commentDao.findByIdAndPostId(mockCommentId, mockPostId))
            .thenReturn(Optional.of(mockComment));

        doNothing()
            .when(commentDao)
            .updateText(anyLong(), anyString());
        
        Comment comment = commentService.updateComment(mockPostId, mockCommentId, mockComment.toBuilder().text("new text").build());
        
        assertEquals("new text", comment.getText());
        assertThrows(ApiServiceException.class, () -> {
            Comment newComment = Comment.builder()
                .id(2L)
                .postId(2L)
                .text("new text")
                .build();
            commentService.updateComment(mockPostId, mockCommentId, newComment);
        });
    }

    @Test
    public void deleteByIdAndPostId() {
        doNothing()
            .when(commentDao)
            .deleteByIdAndPostId(anyLong(), anyLong());
        
        assertDoesNotThrow(() -> commentService.deleteByIdAndPostId(mockCommentId, mockPostId));
    }
}
