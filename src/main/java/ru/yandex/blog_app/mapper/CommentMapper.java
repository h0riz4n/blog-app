package ru.yandex.blog_app.mapper;

import java.util.List;

import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.NullValuePropertyMappingStrategy;

import ru.yandex.blog_app.model.domain.Comment;
import ru.yandex.blog_app.model.dto.CommentDto;

@Mapper(
    componentModel = MappingConstants.ComponentModel.SPRING,
    nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
)
public interface CommentMapper {

    CommentDto toDto(Comment entity);

    List<CommentDto> toDto(List<Comment> entities);

    Comment toEntity(CommentDto dto);
}
