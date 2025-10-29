package ru.yandex.blog_app.service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import ru.yandex.blog_app.exception.ApiServiceException;
import ru.yandex.blog_app.model.entity.PostEntity;
import ru.yandex.blog_app.model.filter.PostFilterModel;
import ru.yandex.blog_app.repository.PostRepository;
import ru.yandex.blog_app.repository.specification.PostSpecification;

@Service
@RequiredArgsConstructor
public class PostService {

    private final TagService tagService;
    private final FileService fileService;
    private final CommentService commentService;

    private final PostRepository postRepo;
    
    public PostEntity create(PostEntity post) {
        post.getTags().forEach(tag -> tag.setPost(post));
        return postRepo.save(post);
    }
    
    public PostEntity getById(Long id) {
        return postRepo.findById(id)
            .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND, "Пост не найден"));
    }

    @Transactional
    public PostEntity updateById(Long id, PostEntity newPost) {
        PostEntity post = postRepo.findById(id)
            .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND, "Пост не найден"));
        validateOnUpdate(id, post);
        post.setText(newPost.getText());
        return postRepo.save(post);
    }

    public Page<PostEntity> getAllByTitleAndTags(String search, Integer pageNumber, Integer pageSize) {
        List<String> words = Arrays.stream(search.trim().split(" "))
            .filter(Predicate.not(String::isBlank))
            .toList();
        
        PostFilterModel filter = PostFilterModel.builder()
            .tags(getTags(words))
            .title(getTitle(words))
            .build();

        Page<PostEntity> posts = postRepo.findAll(new PostSpecification(filter), PageRequest.of(pageNumber, pageSize));
        fetchTagsAndComments(posts.getContent());

        return posts;
    }

    public void deleteById(Long id) {
        PostEntity post = postRepo.findById(id)
            .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND, "Пост не найден"));

        Optional.ofNullable(post.getFileName())
            .filter(Predicate.not(String::isBlank))
            .ifPresent(fileService::delete);

        postRepo.delete(post);
    }

    public Long like(Long id) {
        PostEntity post = postRepo.findById(id)
            .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND, "Пост не найден"));
        post.setLikesCount(post.getLikesCount() + 1);
        return postRepo.save(post).getLikesCount();
    }

    @Transactional
    public String uploadPostImage(Long id, MultipartFile file) {
        PostEntity post = postRepo.findById(id)
            .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND, "Пост не найден"));
        
        Optional.ofNullable(post.getFileName())
            .filter(Predicate.not(String::isBlank))
            .ifPresent(fileService::delete);
    
        post.setFileName(fileService.upload(file));
        return postRepo.save(post).getFileName();
    }

    public byte[] downloadPostImage(Long id) {
        PostEntity post = postRepo.findById(id)
            .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND, "Пост не найден"));

        String fileName = Optional.ofNullable(post.getFileName())
            .filter(Predicate.not(String::isBlank))
            .orElseThrow(() -> new ApiServiceException(HttpStatus.NOT_FOUND, "Изображениe у поста отсутствует"));

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

    private String getTitle(List<String> words) {
        return words.stream()
            .filter(txt -> !txt.startsWith("#"))
            .collect(Collectors.joining(" "));
    }

    private void fetchTagsAndComments(List<PostEntity> posts) {
        var tags = tagService.getAllByPostIn(posts);
        var comments = commentService.getAllByPostIn(posts);
        posts.forEach(post -> {
            post.setTags(tags.stream().filter(tag -> tag.getPost().equals(post)).toList());
            post.setComments(comments.stream().filter(comment -> comment.getPost().equals(post)).toList());
        });
    }

    private void validateOnUpdate(Long id, PostEntity post) {
        if (!post.getId().equals(id)) {
            throw new ApiServiceException(HttpStatus.BAD_REQUEST, "Не совпадают идентификаторы поста в теле и в пути");
        }
    }
}
