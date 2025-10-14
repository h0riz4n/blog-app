package ru.yandex.blog_app.model.dto;

import java.io.Serializable;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonView;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;
import ru.yandex.blog_app.model.view.PostView;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class PostDto implements Serializable {
    
    @JsonView({ PostView.Detail.class, PostView.Update.class })
    @NotNull(groups = { PostView.Update.class })
    private Long id;

    @NotBlank(groups = { PostView.Create.class, PostView.Update.class })
    @Size(max = 50, groups = { PostView.Create.class, PostView.Update.class })
    @JsonView({ PostView.Summary.class, PostView.Update.class })
    private String title;

    @NotBlank(groups = { PostView.Create.class, PostView.Update.class })
    @JsonView({ PostView.Summary.class, PostView.Update.class })
    private String text;

    @Default
    @JsonView({ PostView.Detail.class })
    private Long likesCount = 0L;

    @NotEmpty(groups = { PostView.Create.class, PostView.Update.class })
    @JsonView({ PostView.Summary.class, PostView.Update.class })
    private List<@Size(max = 50) String> tags;

    @Default
    @JsonView({ PostView.Detail.class })
    private Long commentsCount = 0L;
}
