package ru.yandex.blog_app.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import ru.yandex.blog_app.model.entity.CommentEntity;
import ru.yandex.blog_app.model.entity.PostEntity;
import ru.yandex.blog_app.model.entity.TagEntity;
import ru.yandex.blog_app.repository.PostRepository;
import ru.yandex.blog_app.repository.specification.PostSpecification;

@ExtendWith(MockitoExtension.class)
public class PostServiceTest {

    @Mock
    private TagService tagService;

    @Mock
    private FileService fileService;

    @Mock
    private CommentService commentService;

    @Mock
    private PostRepository postRepo;

    @InjectMocks
    private PostService postService;

    private PostEntity mockPost;
    private TagEntity mockTag;
    private CommentEntity mockComment;

    @BeforeEach
    void setUp() {
        this.mockPost = PostEntity.builder()
            .id(1L)
            .text("text")
            .title("title")
            .build();
        this.mockComment = CommentEntity.builder()
            .id(1L)
            .post(mockPost)
            .text("comment")
            .build();
        this.mockTag = TagEntity.builder()
            .id(1L)
            .post(mockPost)
            .text("tag")
            .build();
    }

    @AfterEach
    void afterEach() {
        this.mockPost = null;
        this.mockTag = null;
        this.mockComment = null;
    }

    @Test
    public void testCreate() {
        var newPost = mockPost.toBuilder()
            .tags(List.of(mockTag))
            .build();

        when(postRepo.save(newPost))
            .thenReturn(newPost);

        var createdPost = postService.create(newPost);
        assertEquals(newPost, createdPost);
    }

    @Test
    public void testGetById() {
        Long id = mockPost.getId();

        when(postRepo.findById(id))
            .thenReturn(Optional.of(mockPost));

        var actualPost = postService.getById(id);
        assertEquals(mockPost, actualPost);
    }

    @Test
    public void testUpdateById() {
        Long id = mockPost.getId();
        var newPost = mockPost.toBuilder().text("new text").build();

        when(postRepo.findById(id))
            .thenReturn(Optional.of(mockPost));
        
        when(postRepo.save(newPost))
            .thenReturn(newPost);

        var actualPost = postService.updateById(id, newPost);
        assertEquals(newPost, actualPost);
        assertEquals(newPost.getText(), actualPost.getText());
    }

    @Test
    public void testGetAllByTitleAndTags() {
        String search = "title #tag";
        Integer pageNumber = 0;
        Integer pageSize = 10;

        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        var expectedPosts = List.of(mockPost);

        when(postRepo.findAll(any(PostSpecification.class), any(Pageable.class)))
            .thenReturn(new PageImpl<>(expectedPosts, pageable, 1));
        
        when(tagService.getAllByPostIn(expectedPosts))
            .thenReturn(List.of(mockTag));
        
        when(commentService.getAllByPostIn(expectedPosts))
            .thenReturn(List.of(mockComment));

        var posts = postService.getAllByTitleAndTags(search, pageNumber, pageSize);

        assertFalse(posts.getContent().isEmpty());
        assertTrue(posts.getContent().size() == 1);
        assertEquals(mockPost.getId(), posts.getContent().getFirst().getId());
    }
}   
