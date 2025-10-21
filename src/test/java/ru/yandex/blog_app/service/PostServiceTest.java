package ru.yandex.blog_app.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import ru.yandex.blog_app.dao.PostDao;
import ru.yandex.blog_app.exception.ApiServiceException;
import ru.yandex.blog_app.model.domain.Post;
import ru.yandex.blog_app.model.domain.Tag;
import ru.yandex.blog_app.model.dto.Page;
import ru.yandex.blog_app.service.impl.PostServiceImpl;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    private FileService fileService;

    @Mock
    private CommentService commentService;

    @Mock
    private TagService tagService;

    @Mock
    private PostDao postDao;

    @InjectMocks
    private PostServiceImpl postService;

    private Long mockPostId;
    private Tag mockTag;
    private Post mockPost;

    @BeforeEach
    public void setUp() {
        this.mockPostId = 1L;
        this.mockTag = Tag.builder()
            .id(1L)
            .text("tag1")
            .postId(mockPostId)
            .build();
        this.mockPost = Post.builder()
            .likesCount(0L)
            .commentsCount(0L)
            .title("title")
            .text("text")
            .tags(List.of(mockTag))
            .build();
    }

    @Test
    public void postService_create_returnSavedPost() {
        List<Tag> mockTags = List.of(Tag.builder().text("tag1").build());

        when(postDao.save(mockPost))
            .thenReturn(mockPost.toBuilder().id(mockPostId).build());

        when(tagService.create(mockTags))
            .thenReturn(
                mockTags.stream()
                    .peek(tag -> tag.setId(1L))
                    .toList()
            );

        Post post = postService.create(mockPost);

        assertEquals(Long.valueOf(mockPostId), post.getId());
        assertEquals(List.of(mockTag), post.getTags());
    }

    @Test
    public void postService_getAll_returnPageOfPosts() {
        Post mockPost = this.mockPost.toBuilder()
            .id(mockPostId)
            .tags(Collections.emptyList())
            .build();

        String search = "test #tag1";
        Integer pageNumber = 0;
        Integer pageSize = 1;

        when(postDao.findAll("test", List.of("tag1"), pageNumber, pageSize))
            .thenReturn(
                Page.<Post>builder()
                    .hasNext(true)
                    .hasPrev(false)
                    .lastPage(1L)
                    .posts(List.of(mockPost))
                    .build()
            );

        when(tagService.getAllByPostIds(List.of(1L)))
            .thenReturn(Map.of(1L, List.of(mockTag)));

        when(commentService.commentsCount(List.of(1L)))
            .thenReturn(Map.of(0L, 0L));

        Page<Post> posts = postService.getAll(search, pageNumber, pageSize);

        assertEquals(
            false, 
            posts.getPosts().isEmpty()
        );

        assertEquals(
            List.of(
                mockPost.toBuilder()
                    .tags(List.of(mockTag))
                    .build()
            ),
            posts.getPosts()
        );
    }

    @Test
    public void postService_getById_returnCorrectPost() {
        Post mockPost = this.mockPost.toBuilder()
            .id(mockPostId)
            .build();

        when(postDao.findById(1L))
            .thenReturn(Optional.of(mockPost));

        Post post = postService.getById(1L);

        assertEquals(mockPost.getId(), post.getId());
    }

    @Test
    public void postService_getById_throwsApiServiceException() {
        when(postDao.findById(1L))
            .thenReturn(Optional.empty());
        
        assertThrows(ApiServiceException.class, () -> postService.getById(1L));
    }

    @Test
    public void postService_updateById_returnUpdatedPost() {
        String mockText = "new text";
        Post mockPost = this.mockPost.toBuilder().id(mockPostId).text("old text").build();

        when(postDao.findById(1L))
            .thenReturn(Optional.of(mockPost));

        doNothing()
            .when(postDao)
            .updateText(anyLong(), anyString());

        postService.updateById(mockPostId, mockPost.toBuilder().text(mockText).build());

        assertEquals(mockText, mockPost.getText());
        assertThrows(ApiServiceException.class, () -> postService.updateById(2L, mockPost));
    }

    @Test
    public void postService_deleteById_notThrowsException() {
        doNothing()
            .when(postDao)
            .deleteById(anyLong());
        
        assertDoesNotThrow(() -> postService.deleteById(anyLong()));
    }

    @Test
    public void postService_like_returnLikedPost() {
        Long mockLikesCount = 0L;
        Post mockPost = this.mockPost.toBuilder()
            .id(mockPostId)
            .build();

        when(postDao.findById(1L))
            .thenReturn(Optional.of(mockPost));
        
        doNothing()
            .when(postDao)
            .updateLikesCount(anyLong(), anyLong());
        
        Post post = postService.like(mockPostId);

        assertEquals(mockLikesCount + 1, post.getLikesCount().longValue());
    }

    @Test
    public void postService_uploadPostImage_returnFileName() {
        MultipartFile mockFile = mock(MultipartFile.class);
        Post mockPost = this.mockPost.toBuilder()
            .id(mockPostId)
            .build();
        String mockFileName = "testFileName";

        when(postDao.findById(1L))
            .thenReturn(Optional.of(mockPost));

        when(fileService.upload(mockFile))
            .thenReturn(mockFileName);

        doNothing()
            .when(postDao)
            .updateFileName(anyLong(), anyString());

        assertEquals(mockFileName, postService.uploadPostImage(mockPostId, mockFile));
        assertThrows(ApiServiceException.class, () -> postService.uploadPostImage(2L, mockFile));
    }

    @Test
    public void postService_downloadPostImage_returnBytesOfFile() throws IOException {
        Resource mockResource = mock(Resource.class);
        String mockFileName = "testFileName";
        byte[] mockBytes = new byte[0];

        when(postDao.findFileNameById(mockPostId))
            .thenReturn(Optional.of(mockFileName));

        when(mockResource.getContentAsByteArray())
            .thenReturn(mockBytes);

        when(fileService.download(mockFileName))
            .thenReturn(mockResource);

        assertEquals(mockBytes, postService.downloadPostImage(mockPostId));
    }
}
