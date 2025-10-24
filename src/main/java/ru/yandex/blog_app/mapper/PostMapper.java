package ru.yandex.blog_app.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import ru.yandex.blog_app.model.dto.PostDto;
import ru.yandex.blog_app.model.entity.CommentEntity;
import ru.yandex.blog_app.model.entity.PostEntity;
import ru.yandex.blog_app.model.entity.TagEntity;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class PostMapper {

    @Mapping(target = "tags", source = "dto.tags", qualifiedByName = "toTags")
    public abstract PostEntity toEntity(PostDto dto);

    @Mapping(target = "commentsCount", source = "entity.comments", qualifiedByName = "countComments")
    @Mapping(target = "tags", source = "entity.tags", qualifiedByName = "toTagsText")
    public abstract PostDto toDto(PostEntity entity);

    @Named("toTags")
    public List<TagEntity> toTags(List<String> tagsText) {
        return tagsText.stream()
            .map(tag -> TagEntity.builder().text(tag).build())
            .toList();
    }

    @Named("countComments")
    public int countComments(List<CommentEntity> comments) {
        return comments.size();
    }

    @Named("toTagsText")
    public List<String> toTagsText(List<TagEntity> tags) {
        return tags.stream()
            .map(TagEntity::getText)
            .toList();
    }
}
