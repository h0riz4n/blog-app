package ru.yandex.blog_app.service.impl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.dao.PostDao;
import ru.yandex.blog_app.exception.ApiServiceException;
import ru.yandex.blog_app.model.domain.Post;
import ru.yandex.blog_app.model.domain.Tag;
import ru.yandex.blog_app.model.dto.Page;
import ru.yandex.blog_app.service.CommentService;
import ru.yandex.blog_app.service.FileService;
import ru.yandex.blog_app.service.PostService;
import ru.yandex.blog_app.service.TagService;

@Service
@RequiredArgsConstructor
public class PostServiceImpl implements PostService {

    private final CommentService commentService;
    private final TagService tagService;
    private final FileService fileService;

    private final PostDao postDao;

    @Override
    @Transactional
    public Post create(Post post) {
        Post savedPost = postDao.save(post);
        post.getTags().forEach(tag -> tag.setPostId(savedPost.getId()));
        tagService.create(savedPost.getTags());
        return savedPost;
    }

    @Override
    public Page<Post> getAll(String search, Integer pageNumber, Integer pageSize) {
        List<String> words = Arrays.stream(search.split(" "))
            .filter(Predicate.not(String::isBlank))
            .toList();

        Page<Post> pageOfPosts = postDao.findAll(getTitleSearch(words), getTags(words), pageNumber, pageSize);
        List<Long> postIds = pageOfPosts.getPosts().stream().map(Post::getId).toList();

        Map<Long, Long> commentsCount = commentService.commentsCount(postIds);
        Map<Long, List<Tag>> tagsMap = tagService.getAllByPostIds(pageOfPosts.getPosts().stream().map(Post::getId).toList());

        pageOfPosts.getPosts().forEach(post -> {
            post.setTags(tagsMap.getOrDefault(post.getId(), Collections.emptyList()));
            post.setCommentsCount(commentsCount.getOrDefault(post.getId(), 0L));
        });

        return pageOfPosts;
    }
    
    @Override
    public Post getById(Long id) {
        return postDao.findById(id)
            .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND, "Пост не найден"));
    }

    @Override
    @Transactional
    public Post updateById(Long id, Post newPost) {
        Post post = getById(id);

        validateOnUpdate(id, newPost);

        post.setText(newPost.getText());
        postDao.updateText(post.getId(), post.getText());
        return post;
    }

    @Override
    public void deleteById(Long id) {
        postDao.deleteById(id);
    }

    @Override
    @Transactional
    public Post like(Long id) {
        Post post = getById(id);
        post.setLikesCount(post.getLikesCount() + 1);
        postDao.updateLikesCount(post.getId(), post.getLikesCount());
        return post;
    }

    @Override
    @Transactional
    public String uploadPostImage(Long id, MultipartFile file) {
        postDao.findById(id).orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND, "Пост не найден"));
        
        String fileName = fileService.upload(file);
        postDao.updateFileName(id, fileName);
        return fileName;
    }

    @Override
    public byte[] downloadPostImage(Long id) {
        String fileName = postDao.findFileNameById(id)
            .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND, "Изображении у поста отсутствует"));
        try {
            return fileService.download(fileName).getContentAsByteArray();
        } catch (IOException ex) {
            throw new ApiServiceException(HttpStatus.BAD_REQUEST, ex.getMessage());
        }
    }

    private List<String> getTags(List<String> words) {
        return words.stream()
            .filter(txt -> txt.startsWith("#"))
            .map(txt -> txt.substring(1))
            .toList();
    } 

    private String getTitleSearch(List<String> words) {
        return words.stream()
            .filter(txt -> !txt.startsWith("#"))
            .collect(Collectors.joining(" "));
    }

    private void validateOnUpdate(Long id, Post post) {
        if (!post.getId().equals(id)) {
            throw new ApiServiceException(HttpStatus.BAD_REQUEST, "Не совпадают идентификаторы поста в теле и в пути");
        }


    }
}
