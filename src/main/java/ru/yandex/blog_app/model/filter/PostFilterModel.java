package ru.yandex.blog_app.model.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder.Default;

@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostFilterModel {

    private String title;

    @Default
    private List<String> tags = new ArrayList<>();

    public Optional<String> getTitle() {
        return Optional.ofNullable(title);
    }

    public Optional<List<String>> getTags() {
        return Optional.ofNullable(tags);
    }
}
