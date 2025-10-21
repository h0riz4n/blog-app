package ru.yandex.blog_app.mapper;

import java.util.List;
import java.util.Optional;


import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import ru.yandex.blog_app.model.domain.Post;
import ru.yandex.blog_app.model.domain.Tag;
import ru.yandex.blog_app.model.dto.Page;
import ru.yandex.blog_app.model.dto.PostDto;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface PostMapper {

    @Mapping(target = "fileName", ignore = true)
    @Mapping(target = "tags", source = "tags", qualifiedByName = "toTags")
    Post toEntity(PostDto dto);

    @Mapping(target = "tags", source = "tags", qualifiedByName = "toTagText")
    PostDto toDto(Post entity);

    default Page<PostDto> toPage(Page<Post> entities) {
        List<PostDto> dtos = entities.getPosts().stream()
            .map(this::toDtoTruncated)
            .toList();
        return new Page<>(dtos, entities.getHasPrev(), entities.getHasNext(), entities.getLastPage());
    };

    @Mapping(target = "tags", source = "tags", qualifiedByName = "toTagText")
    @Mapping(target = "text", source = "text", qualifiedByName = "truncateText")
    PostDto toDtoTruncated(Post entity);

    @Named("truncateText")
    default String truncateText(String text) {
        return Optional.ofNullable(text)
            .map(txt -> txt.length() > 128 ? txt.substring(0, 128) + "..." : txt)
            .orElse(null);
    }

    @Named("toTags")
    default List<Tag> toTag(List<String> tags) {
        return tags.stream().map(tag -> Tag.builder().text(tag).build()).toList();
    }

    @Named("toTagText")
    default List<String> toTagText(List<Tag> tags) {
        return tags.stream().map(Tag::getText).toList();
    }
}
