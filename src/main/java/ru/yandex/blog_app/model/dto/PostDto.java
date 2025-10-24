package ru.yandex.blog_app.model.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.yandex.blog_app.model.view.PostView;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostDto {

    @JsonView({ PostView.Details.class, PostView.Modify.class })
    @NotNull(groups = { PostView.Modify.class })
    private Long id;

    @NotBlank(groups = { PostView.Create.class, PostView.Modify.class })
    @Size(max = 50, groups = { PostView.Create.class, PostView.Modify.class })
    @JsonView({ PostView.Details.class, PostView.Create.class, PostView.Modify.class })
    private String title;

    @NotBlank(groups = { PostView.Create.class, PostView.Modify.class })
    @JsonView({ PostView.Details.class, PostView.Create.class, PostView.Modify.class })
    private String text;

    @Default
    @JsonView({ PostView.Details.class })
    private Long likesCount = 0L;

    @NotEmpty(groups = { PostView.Create.class, PostView.Modify.class })
    @JsonView({ PostView.Details.class, PostView.Create.class, PostView.Modify.class })
    private List<@Size(max = 50) String> tags;

    @Default
    @JsonView({ PostView.Details.class })
    private Long commentsCount = 0L;
}
