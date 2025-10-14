package ru.yandex.blog_app.model.domain;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;
import lombok.EqualsAndHashCode;

@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(of = "id")
public class Post implements Serializable {

    private Long id;

    @NotBlank
    private String title;

    @NotBlank
    private String text;

    @Default
    @NotNull
    private Long likesCount = 0L;

    @NotEmpty
    @Default
    private List<Tag> tags = new ArrayList<>();

    @Default
    private Long commentsCount = 0L;

    @JsonIgnore
    private String fileName;
}
