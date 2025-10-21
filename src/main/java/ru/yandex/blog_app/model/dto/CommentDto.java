package ru.yandex.blog_app.model.dto;

import com.fasterxml.jackson.annotation.JsonView;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.blog_app.model.view.CommentView;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class CommentDto {

    @NotNull(groups = { CommentView.Update.class })
    @Positive(groups = { CommentView.Update.class })
    @JsonView({ CommentView.Details.class, CommentView.Update.class })
    private Long id;

    @NotNull(groups = { CommentView.Create.class, CommentView.Update.class })
    @Positive(groups = { CommentView.Create.class, CommentView.Update.class })
    @JsonView({ CommentView.Summary.class, CommentView.Create.class, CommentView.Update.class })
    private Long postId;

    @NotEmpty(groups = { CommentView.Create.class, CommentView.Update.class })
    @JsonView({ CommentView.Summary.class, CommentView.Create.class, CommentView.Update.class })
    private String text;
}
