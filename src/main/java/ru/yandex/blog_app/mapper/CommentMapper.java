package ru.yandex.blog_app.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;
import org.mapstruct.Named;
import org.mapstruct.NullValuePropertyMappingStrategy;

import ru.yandex.blog_app.model.dto.CommentDto;
import ru.yandex.blog_app.model.entity.CommentEntity;
import ru.yandex.blog_app.model.entity.PostEntity;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public abstract class CommentMapper {

    @Mapping(target = "postId", source = "entity.post.id")
    public abstract CommentDto toDto(CommentEntity entity);

    @Mapping(target = "post", source = "dto.postId", qualifiedByName = "toPostEntity")
    public abstract CommentEntity toEntity(CommentDto dto);
    
    public abstract List<CommentDto> toDto(List<CommentEntity> entities);

    @Named("toPostEntity")
    public PostEntity toPostEntity(Long id) {
        return PostEntity.builder().id(id).build();
    }
}
